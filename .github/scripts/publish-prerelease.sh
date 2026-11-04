#!/usr/bin/env bash
# CI builds/tests source, then publishes ONLY the compatible signed draft assets.
# The signing private key stays outside GitHub. Never publish a stable release here.
set -euo pipefail
trap 'echo "Pre-release verification failed at line $LINENO; publication stopped." >&2' ERR

: "${GH_REPO:?Missing repository}"
: "${RELEASE_TAG:?Missing tag}"
: "${ANDROID_HOME:?Missing Android SDK}"
[[ "$RELEASE_TAG" =~ ^v[0-9]+\.[0-9]+\.[0-9]+-[a-zA-Z0-9.-]+$ ]] || {
  echo "Only test-version tags can be published automatically." >&2
  exit 1
}

metadata="$(gh release view "$RELEASE_TAG" --repo "$GH_REPO" --json tagName,isDraft,isPrerelease --jq '[.tagName, .isDraft, .isPrerelease] | @tsv')"
[[ "$metadata" == "$RELEASE_TAG"$'\ttrue\ttrue' ]] || {
  echo "Expected an unpublished pre-release draft for this exact tag; refusing to modify an existing release." >&2
  exit 1
}
notes_length="$(gh release view "$RELEASE_TAG" --repo "$GH_REPO" --json body --jq '.body | length')"
[[ "$notes_length" =~ ^[1-9][0-9]*$ ]] || { echo "Release notes are required." >&2; exit 1; }

ASSET_NAME="RotomPedia-${RELEASE_TAG}.apk"
echo "Verified pre-release draft and notes: $RELEASE_TAG"
ASSET_DIR="$(mktemp -d)"
gh release download "$RELEASE_TAG" --repo "$GH_REPO" --pattern "$ASSET_NAME" --pattern "$ASSET_NAME.sha256" --dir "$ASSET_DIR"
read -r expected_hash checksum_name extra < "$ASSET_DIR/$ASSET_NAME.sha256"
[[ "$expected_hash" =~ ^[a-f0-9]{64}$ && "$checksum_name" == "$ASSET_NAME" && -z "$extra" ]]
actual_hash="$(sha256sum "$ASSET_DIR/$ASSET_NAME" | cut -d ' ' -f 1)"
[[ "$actual_hash" == "$expected_hash" ]] || { echo "APK checksum mismatch." >&2; exit 1; }
echo "Verified APK checksum: $actual_hash"

APKSIGNER="$(find -L "$ANDROID_HOME/build-tools" -type f -name apksigner -executable | sort -V | tail -n 1)"
AAPT="$(find -L "$ANDROID_HOME/build-tools" -type f -name aapt -executable | sort -V | tail -n 1)"
[[ -x "$APKSIGNER" && -x "$AAPT" ]] || { echo "Executable Android verification tools are missing." >&2; exit 1; }
echo "Using apksigner: $APKSIGNER; aapt: $AAPT"
certificates="$("$APKSIGNER" verify --print-certs "$ASSET_DIR/$ASSET_NAME")"
[[ "$(grep -cE '^Signer #[0-9]+ certificate SHA-256 digest:' <<< "$certificates")" == 1 ]]
grep -Fxq 'Signer #1 certificate SHA-256 digest: 5831c434c9ab62110644c025634679dba6101a99621eac2906f1bbcba1a3f1aa' <<< "$certificates" || {
  echo "Signing certificate differs from the previously published compatible APK. Publication stopped." >&2
  exit 1
}
echo "Verified compatible signing certificate."

apk_metadata() {
  "$AAPT" dump badging "$1" | sed -n "s/^package: name='\([^']*\)' versionCode='\([^']*\)' versionName='\([^']*\)'.*/\1|\2|\3/p"
}
signed_metadata="$(apk_metadata "$ASSET_DIR/$ASSET_NAME")"
ci_metadata="$(apk_metadata RotomPedia/app/build/outputs/apk/debug/app-debug.apk)"
echo "Signed APK metadata: $signed_metadata; CI metadata: $ci_metadata"
[[ -n "$signed_metadata" && "$signed_metadata" == "$ci_metadata" ]] || {
  echo "Signed APK package/version does not match the verified CI build." >&2
  exit 1
}
IFS='|' read -r package_name version_code version_name <<< "$signed_metadata"
[[ "$package_name" == com.duoc.rotompedia && "$version_code" =~ ^[1-9][0-9]*$ && "$version_name" == "${RELEASE_TAG#v}" ]]

echo "Verified signed APK: $signed_metadata; SHA-256: $actual_hash"
gh release edit "$RELEASE_TAG" --repo "$GH_REPO" --draft=false --prerelease --latest=false
gh release view "$RELEASE_TAG" --repo "$GH_REPO" --json url,isDraft,isPrerelease
