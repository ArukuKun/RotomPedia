package com.duoc.rotompediabackend

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/leaders")
class GymLeaderController(private val repository: GymLeaderRepository) {

    // URL: http://localhost:8080/api/leaders
    @GetMapping
    fun getAllLeaders(): List<GymLeader> {
        return repository.findAll()
    }

    // URL: http://localhost:8080/api/leaders/region/{regionName}
    @GetMapping("/region/{regionName}")
    fun getLeadersByRegion(@PathVariable regionName: String): List<GymLeader> {
        return repository.findByRegionIgnoreCase(regionName)
    }

    // Carga de datos iniciales completa de todas las generaciones
    @Bean
    fun initDatabase() = CommandLineRunner {
        repository.deleteAll()
        repository.saveAll(listOf(
            // ==================== KANTO ====================
            GymLeader(
                name = "Brock",
                region = "Kanto",
                typeSpecialty = "rock",
                badgeName = "Medalla Roca",
                city = "Ciudad Plateada",
                signaturePokemon = "Onix",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/brock.png",
                description = "El sólido entrenador tipo Roca. Firme y bondadoso."
            ),
            GymLeader(
                name = "Misty",
                region = "Kanto",
                typeSpecialty = "water",
                badgeName = "Medalla Cascada",
                city = "Ciudad Celeste",
                signaturePokemon = "Starmie",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/misty.png",
                description = "La bella y temible sirena experta en Pokémon de tipo Agua."
            ),
            GymLeader(
                name = "Lt. Surge",
                region = "Kanto",
                typeSpecialty = "electric",
                badgeName = "Medalla Trueno",
                city = "Ciudad Carmín",
                signaturePokemon = "Raichu",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/ltsurge.png",
                description = "El rayo americano. Experto militar con un potente arsenal eléctrico."
            ),
            GymLeader(
                name = "Erika",
                region = "Kanto",
                typeSpecialty = "grass",
                badgeName = "Medalla Arcoíris",
                city = "Ciudad Azulona",
                signaturePokemon = "Vileplume",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/erika.png",
                description = "La amante de la naturaleza y maestra del arreglo floral."
            ),
            GymLeader(
                name = "Koga",
                region = "Kanto",
                typeSpecialty = "poison",
                badgeName = "Medalla Alma",
                city = "Ciudad Fucsia",
                signaturePokemon = "Weezing",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/koga.png",
                description = "El maestro ninja venenoso del dojo de Fucsia."
            ),
            GymLeader(
                name = "Sabrina",
                region = "Kanto",
                typeSpecialty = "psychic",
                badgeName = "Medalla Pantano",
                city = "Ciudad Azafrán",
                signaturePokemon = "Alakazam",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/sabrina.png",
                description = "La maestra de los poderes psicoquinéticos."
            ),
            GymLeader(
                name = "Blaine",
                region = "Kanto",
                typeSpecialty = "fire",
                badgeName = "Medalla Volcán",
                city = "Isla Canela",
                signaturePokemon = "Magmar",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/blaine.png",
                description = "El apasionado maestro de los acertijos ardientes."
            ),
            GymLeader(
                name = "Giovanni",
                region = "Kanto",
                typeSpecialty = "ground",
                badgeName = "Medalla Tierra",
                city = "Ciudad Verde",
                signaturePokemon = "Rhydon",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/giovanni.png",
                description = "Líder de gimnasio y mente maestra del Team Rocket."
            ),

            // ==================== JOHTO ====================
            GymLeader(
                name = "Pegaso (Falkner)",
                region = "Johto",
                typeSpecialty = "flying",
                badgeName = "Medalla Céfiro",
                city = "Ciudad Malva",
                signaturePokemon = "Pidgeotto",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/falkner.png",
                description = "El elegante jinete del viento y guardián de los cielos."
            ),
            GymLeader(
                name = "Antón (Bugsy)",
                region = "Johto",
                typeSpecialty = "bug",
                badgeName = "Medalla Colmena",
                city = "Pueblo Azalea",
                signaturePokemon = "Scyther",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/bugsy.png",
                description = "La enciclopedia andante de los Pokémon bicho."
            ),
            GymLeader(
                name = "Blanca (Whitney)",
                region = "Johto",
                typeSpecialty = "normal",
                badgeName = "Medalla Planicie",
                city = "Ciudad Trigal",
                signaturePokemon = "Miltank",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/whitney.png",
                description = "La encantadora pero temida entrenadora del Miltank rodante."
            ),
            GymLeader(
                name = "Morti (Morty)",
                region = "Johto",
                typeSpecialty = "ghost",
                badgeName = "Medalla Niebla",
                city = "Ciudad Iris",
                signaturePokemon = "Gengar",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/morty.png",
                description = "El místico vidente que busca al Pokémon legendario Ho-Oh."
            ),
            GymLeader(
                name = "Aníbal (Chuck)",
                region = "Johto",
                typeSpecialty = "fighting",
                badgeName = "Medalla Tormenta",
                city = "Ciudad Orquídea",
                signaturePokemon = "Poliwrath",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/chuck.png",
                description = "El guerrero de puños de acero que entrena bajo cascadas."
            ),
            GymLeader(
                name = "Yasmina (Jasmine)",
                region = "Johto",
                typeSpecialty = "steel",
                badgeName = "Medalla Mineral",
                city = "Ciudad Olivo",
                signaturePokemon = "Steelix",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/jasmine.png",
                description = "La tímida chica de la defensa férrea."
            ),
            GymLeader(
                name = "Fredo (Pryce)",
                region = "Johto",
                typeSpecialty = "ice",
                badgeName = "Medalla Glaciar",
                city = "Pueblo Caoba",
                signaturePokemon = "Piloswine",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/pryce.png",
                description = "El veterano del invierno eterno y maestro de la experiencia."
            ),
            GymLeader(
                name = "Débora (Clair)",
                region = "Johto",
                typeSpecialty = "dragon",
                badgeName = "Medalla Dragón",
                city = "Ciudad Endrino",
                signaturePokemon = "Kingdra",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/clair.png",
                description = "La orgullosa doma-dragones del clan de Endrino."
            ),

            // ==================== HOENN ====================
            GymLeader(
                name = "Petra (Roxanne)",
                region = "Hoenn",
                typeSpecialty = "rock",
                badgeName = "Medalla Piedra",
                city = "Ciudad Férrica",
                signaturePokemon = "Nosepass",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/roxanne.png",
                description = "La brillante graduada de la Escuela de Entrenadores."
            ),
            GymLeader(
                name = "Marcial (Brawly)",
                region = "Hoenn",
                typeSpecialty = "fighting",
                badgeName = "Medalla Puño",
                city = "Pueblo Azuliza",
                signaturePokemon = "Makuhita",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/brawly.png",
                description = "El surfista que domina las grandes olas y el combate físico."
            ),
            GymLeader(
                name = "Erico (Wattson)",
                region = "Hoenn",
                typeSpecialty = "electric",
                badgeName = "Medalla Dinamo",
                city = "Ciudad Malvalona",
                signaturePokemon = "Manectric",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/wattson.png",
                description = "El alegre inventor electrificante de Malvalona."
            ),
            GymLeader(
                name = "Candela (Flannery)",
                region = "Hoenn",
                typeSpecialty = "fire",
                badgeName = "Medalla Calor",
                city = "Pueblo Lavacalda",
                signaturePokemon = "Torkoal",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/flannery.png",
                description = "Una líder fogosa y apasionada que recién asume el gimnasio."
            ),
            GymLeader(
                name = "Norman",
                region = "Hoenn",
                typeSpecialty = "normal",
                badgeName = "Medalla Equilibrio",
                city = "Ciudad Petalia",
                signaturePokemon = "Slaking",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/norman.png",
                description = "El padre del protagonista, modelo de disciplina y fuerza."
            ),
            GymLeader(
                name = "Alana (Winona)",
                region = "Hoenn",
                typeSpecialty = "flying",
                badgeName = "Medalla Pluma",
                city = "Ciudad Arborada",
                signaturePokemon = "Altaria",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/winona.png",
                description = "La danzarina de los vientos que vive en las copas de los árboles."
            ),
            GymLeader(
                name = "Vito y Leti (Tate & Liza)",
                region = "Hoenn",
                typeSpecialty = "psychic",
                badgeName = "Medalla Mente",
                city = "Ciudad Algaria",
                signaturePokemon = "Lunatone & Solrock",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/tate.png",
                description = "Los gemelos telepáticos expertos en batallas dobles."
            ),
            GymLeader(
                name = "Plubio (Wallace)",
                region = "Hoenn",
                typeSpecialty = "water",
                badgeName = "Medalla Lluvia",
                city = "Arrecípolis",
                signaturePokemon = "Milotic",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/wallace.png",
                description = "El artista del agua y maestro de los concursos Pokémon."
            ),

            // ==================== SINNOH ====================
            GymLeader(
                name = "Roco (Roark)",
                region = "Sinnoh",
                typeSpecialty = "rock",
                badgeName = "Medalla Lignito",
                city = "Ciudad Pirita",
                signaturePokemon = "Cranidos",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/roark.png",
                description = "El joven capataz minero y apasionado de los fósiles."
            ),
            GymLeader(
                name = "Gardenia",
                region = "Sinnoh",
                typeSpecialty = "grass",
                badgeName = "Medalla Bosque",
                city = "Ciudad Vetusta",
                signaturePokemon = "Roserade",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/gardenia.png",
                description = "La intrépida defensora de los bosques y amante de la flora."
            ),
            GymLeader(
                name = "Brega (Maylene)",
                region = "Sinnoh",
                typeSpecialty = "fighting",
                badgeName = "Medalla Adoquín",
                city = "Ciudad Rocavelo",
                signaturePokemon = "Lucario",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/maylene.png",
                description = "La perseverante karateka que busca perfeccionar su espíritu."
            ),
            GymLeader(
                name = "Mananti (Crasher Wake)",
                region = "Sinnoh",
                typeSpecialty = "water",
                badgeName = "Medalla Ciénaga",
                city = "Ciudad Pradera",
                signaturePokemon = "Floatzel",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/crasherwake.png",
                description = "El enérgico luchador enmascarado del Gran Pantano."
            ),
            GymLeader(
                name = "Fantina",
                region = "Sinnoh",
                typeSpecialty = "ghost",
                badgeName = "Medalla Reliquia",
                city = "Ciudad Corazón",
                signaturePokemon = "Mismagius",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/fantina.png",
                description = "La elegante bailarina cosmopolita de los espectros."
            ),
            GymLeader(
                name = "Acerón (Byron)",
                region = "Sinnoh",
                typeSpecialty = "steel",
                badgeName = "Medalla Mina",
                city = "Ciudad Canal",
                signaturePokemon = "Bastiodon",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/byron.png",
                description = "El curtido excavador de Isla Hierro y padre de Roco."
            ),
            GymLeader(
                name = "Inverna (Candice)",
                region = "Sinnoh",
                typeSpecialty = "ice",
                badgeName = "Medalla Carámbano",
                city = "Ciudad Puntaneva",
                signaturePokemon = "Froslass",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/candice.png",
                description = "La enérgica especialista del hielo y la concentración."
            ),
            GymLeader(
                name = "Lectro (Volkner)",
                region = "Sinnoh",
                typeSpecialty = "electric",
                badgeName = "Medalla Faro",
                city = "Ciudad Marina",
                signaturePokemon = "Electivire",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/volkner.png",
                description = "El genio tecnológico que busca un combate que lo entusiasme."
            ),

            // ==================== TESELIA (UNOVA) ====================
            GymLeader(
                name = "Millo, Zeo y Maíz (Cilan)",
                region = "Teselia",
                typeSpecialty = "grass",
                badgeName = "Medalla Trío",
                city = "Ciudad Gres",
                signaturePokemon = "Simisage",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/cilan.png",
                description = "Los trillizos sibaritas que ofrecen un combate a la carta."
            ),
            GymLeader(
                name = "Hiedra (Roxie)",
                region = "Teselia",
                typeSpecialty = "poison",
                badgeName = "Medalla Ponzoña",
                city = "Ciudad Hormigón",
                signaturePokemon = "Scolipede",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/roxie.png",
                description = "La bajista de rock punk más venenosa de Teselia."
            ),
            GymLeader(
                name = "Camus (Burgh)",
                region = "Teselia",
                typeSpecialty = "bug",
                badgeName = "Medalla Élitro",
                city = "Ciudad Porcelana",
                signaturePokemon = "Leavanny",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/burgh.png",
                description = "El artista y pintor inspirado por la belleza de los bichos."
            ),
            GymLeader(
                name = "Camila (Elesa)",
                region = "Teselia",
                typeSpecialty = "electric",
                badgeName = "Medalla Voltio",
                city = "Ciudad Mayólica",
                signaturePokemon = "Zebstrika",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/elesa.png",
                description = "La supermodelo de pasarela con un estilo de combate electrizante."
            ),
            GymLeader(
                name = "Yakón (Clay)",
                region = "Teselia",
                typeSpecialty = "ground",
                badgeName = "Medalla Temblor",
                city = "Ciudad Fayenza",
                signaturePokemon = "Excadrill",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/clay.png",
                description = "El veterano magnate minero que no teme ensuciarse las botas."
            ),
            GymLeader(
                name = "Gerania (Skyla)",
                region = "Teselia",
                typeSpecialty = "flying",
                badgeName = "Medalla Jet",
                city = "Ciudad Loza",
                signaturePokemon = "Swanna",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/skyla.png",
                description = "La simpática piloto de aviones de carga y acróbata del aire."
            ),
            GymLeader(
                name = "Lirio (Drayden)",
                region = "Teselia",
                typeSpecialty = "dragon",
                badgeName = "Medalla Leyenda",
                city = "Ciudad Caolín",
                signaturePokemon = "Haxorus",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/drayden.png",
                description = "El honorable alcalde de Caolín y patriarca de los dragones."
            ),
            GymLeader(
                name = "Ciprián (Marlon)",
                region = "Teselia",
                typeSpecialty = "water",
                badgeName = "Medalla Ola",
                city = "Ciudad Marga",
                signaturePokemon = "Carracosta",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/marlon.png",
                description = "El nadador relajado que fluye libre como las mareas."
            ),

            // ==================== KALOS ====================
            GymLeader(
                name = "Violeta (Viola)",
                region = "Kalos",
                typeSpecialty = "bug",
                badgeName = "Medalla Insecto",
                city = "Ciudad Novarte",
                signaturePokemon = "Vivillon",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/viola.png",
                description = "La fotógrafa profesional que retrata la grandeza diminuta."
            ),
            GymLeader(
                name = "Lino (Grant)",
                region = "Kalos",
                typeSpecialty = "rock",
                badgeName = "Medalla Muro",
                city = "Ciudad Relieve",
                signaturePokemon = "Tyrunt",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/grant.png",
                description = "El escalador de altas cimas y atleta todoterreno."
            ),
            GymLeader(
                name = "Corelia (Korrina)",
                region = "Kalos",
                typeSpecialty = "fighting",
                badgeName = "Medalla Lid",
                city = "Ciudad Yantra",
                signaturePokemon = "Lucario",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/korrina.png",
                description = "La patinadora prodigio y sucesora de la Megaevolución."
            ),
            GymLeader(
                name = "Amaro (Ramos)",
                region = "Kalos",
                typeSpecialty = "grass",
                badgeName = "Medalla Hoja",
                city = "Ciudad Témpera",
                signaturePokemon = "Gogoat",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/ramos.png",
                description = "El sabio jardinero que cuida con paciencia de la naturaleza."
            ),
            GymLeader(
                name = "Lem (Clemont)",
                region = "Kalos",
                typeSpecialty = "electric",
                badgeName = "Medalla Voltaje",
                city = "Ciudad Luminalia",
                signaturePokemon = "Heliolisk",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/clemont.png",
                description = "El joven inventor de la Torre Prisma y genio de la robótica."
            ),
            GymLeader(
                name = "Valeria (Valerie)",
                region = "Kalos",
                typeSpecialty = "fairy",
                badgeName = "Medalla Hada",
                city = "Ciudad Romantis",
                signaturePokemon = "Sylveon",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/valerie.png",
                description = "La diseñadora de alta costura que soñaba con ser un Pokémon."
            ),
            GymLeader(
                name = "Ástrid (Olympia)",
                region = "Kalos",
                typeSpecialty = "psychic",
                badgeName = "Medalla Psique",
                city = "Ciudad Fluxus",
                signaturePokemon = "Meowstic",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/olympia.png",
                description = "La sacerdotisa estelar conectada con el destino cósmico."
            ),
            GymLeader(
                name = "Édel (Wulfric)",
                region = "Kalos",
                typeSpecialty = "ice",
                badgeName = "Medalla Iceberg",
                city = "Ciudad Fractal",
                signaturePokemon = "Avalugg",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/wulfric.png",
                description = "El noble gigante que enseña que el hielo es tan sólido como flexible."
            ),

            // ==================== ALOLA ====================
            GymLeader(
                name = "Kaudan (Hala)",
                region = "Alola",
                typeSpecialty = "fighting",
                badgeName = "Cristal Lucha Z",
                city = "Pueblo Lilii (Melemele)",
                signaturePokemon = "Crabominable",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/hala.png",
                description = "El respetado Kahuna de la isla Melemele."
            ),
            GymLeader(
                name = "Mayla (Olivia)",
                region = "Alola",
                typeSpecialty = "rock",
                badgeName = "Cristal Roca Z",
                city = "Ciudad Konikoni (Akala)",
                signaturePokemon = "Lycanroc",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/olivia.png",
                description = "La carismática y apasionada Kahuna de Akala."
            ),
            GymLeader(
                name = "Denio (Nanu)",
                region = "Alola",
                typeSpecialty = "dark",
                badgeName = "Cristal Siniestro Z",
                city = "Ruta 17 (Ula-Ula)",
                signaturePokemon = "Persian de Alola",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/nanu.png",
                description = "El veterano oficial de policía y Kahuna de Ula-Ula."
            ),
            GymLeader(
                name = "Hela (Hapu)",
                region = "Alola",
                typeSpecialty = "ground",
                badgeName = "Cristal Tierra Z",
                city = "Pueblo Marino (Poni)",
                signaturePokemon = "Mudsdale",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/hapu.png",
                description = "La joven y valiente Kahuna de la mística isla Poni."
            ),

            // ==================== GALAR ====================
            GymLeader(
                name = "Milo",
                region = "Galar",
                typeSpecialty = "grass",
                badgeName = "Medalla Planta",
                city = "Pueblo Hoyuelo",
                signaturePokemon = "Eldegoss",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/milo.png",
                description = "El bondadoso campesino que disfruta combatiendo con una sonrisa."
            ),
            GymLeader(
                name = "Cathy (Nessa)",
                region = "Galar",
                typeSpecialty = "water",
                badgeName = "Medalla Agua",
                city = "Pueblo Amura",
                signaturePokemon = "Drednaw",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/nessa.png",
                description = "Modelo profesional y nadadora de corriente indomable."
            ),
            GymLeader(
                name = "Naboru (Kabu)",
                region = "Galar",
                typeSpecialty = "fire",
                badgeName = "Medalla Fuego",
                city = "Ciudad Pistón",
                signaturePokemon = "Centiskorch",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/kabu.png",
                description = "El veterano del entrenamiento físico y la perseverancia."
            ),
            GymLeader(
                name = "Judith (Bea)",
                region = "Galar",
                typeSpecialty = "fighting",
                badgeName = "Medalla Lucha",
                city = "Pueblo Ladera",
                signaturePokemon = "Sirfetch'd",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/bea.png",
                description = "La prodigio del karate de Galar, fría y calculadora."
            ),
            GymLeader(
                name = "Alistair (Allister)",
                region = "Galar",
                typeSpecialty = "ghost",
                badgeName = "Medalla Fantasma",
                city = "Pueblo Ladera",
                signaturePokemon = "Gengar",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/allister.png",
                description = "El reservado muchacho que susurra a los espíritus tras su máscara."
            ),
            GymLeader(
                name = "Sally (Opal)",
                region = "Galar",
                typeSpecialty = "fairy",
                badgeName = "Medalla Hada",
                city = "Pueblo Plié",
                signaturePokemon = "Alcremie",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/opal.png",
                description = "La matriarca del teatro y buscadora del color rosa."
            ),
            GymLeader(
                name = "Morris (Gordie)",
                region = "Galar",
                typeSpecialty = "rock",
                badgeName = "Medalla Roca",
                city = "Pueblo Auriga",
                signaturePokemon = "Coalossal",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/gordie.png",
                description = "La estrella mediática de las rocas pesadas."
            ),
            GymLeader(
                name = "Roy (Raihan)",
                region = "Galar",
                typeSpecialty = "dragon",
                badgeName = "Medalla Dragón",
                city = "Ciudad Artejo",
                signaturePokemon = "Duraludon",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/raihan.png",
                description = "El rival del Campeón Lionel y maestro de las tormentas de arena."
            ),

            // ==================== PALDEA ====================
            GymLeader(
                name = "Araceli (Katy)",
                region = "Paldea",
                typeSpecialty = "bug",
                badgeName = "Medalla Bicho",
                city = "Pueblo Pirotín",
                signaturePokemon = "Lokix",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/katy.png",
                description = "La repostera artesanal cuyos dulces son tan tiernos como sus bichos."
            ),
            GymLeader(
                name = "Fuco (Brassius)",
                region = "Paldea",
                typeSpecialty = "grass",
                badgeName = "Medalla Planta",
                city = "Pueblo Altamía",
                signaturePokemon = "Sudowoodo",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/brassius.png",
                description = "El artista vanguardista de la 'Extensa Renuncia'."
            ),
            GymLeader(
                name = "e-Nidia (Iono)",
                region = "Paldea",
                typeSpecialty = "electric",
                badgeName = "Medalla Eléctrica",
                city = "Ciudad Leudal",
                signaturePokemon = "Bellibolt",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/iono.png",
                description = "La streamer y estrella de las redes que electrocuta la pantalla."
            ),
            GymLeader(
                name = "Brais (Kofu)",
                region = "Paldea",
                typeSpecialty = "water",
                badgeName = "Medalla Agua",
                city = "Ciudad Cántara",
                signaturePokemon = "Wugtrio",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/kofu.png",
                description = "El chef de alta mar con un corazón tan grande como el océano."
            ),
            GymLeader(
                name = "Laureano (Larry)",
                region = "Paldea",
                typeSpecialty = "normal",
                badgeName = "Medalla Normal",
                city = "Pueblo Mestura",
                signaturePokemon = "Staraptor",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/larry.png",
                description = "El asalariado infatigable que representa a la gente trabajadora."
            ),
            GymLeader(
                name = "Lima (Ryme)",
                region = "Paldea",
                typeSpecialty = "ghost",
                badgeName = "Medalla Fantasma",
                city = "Pueblo Hozkailu",
                signaturePokemon = "Houndstone",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/ryme.png",
                description = "La rapera legendaria que rima con las almas del más allá."
            ),
            GymLeader(
                name = "Tula (Tulip)",
                region = "Paldea",
                typeSpecialty = "psychic",
                badgeName = "Medalla Psíquica",
                city = "Pueblo Alfornada",
                signaturePokemon = "Florges",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/tulip.png",
                description = "La célebre maquilladora que hipnotiza con su magia estética."
            ),
            GymLeader(
                name = "Grusha",
                region = "Paldea",
                typeSpecialty = "ice",
                badgeName = "Medalla Hielo",
                city = "Sierra Napada",
                signaturePokemon = "Altaria",
                imageUrl = "https://play.pokemonshowdown.com/sprites/trainers/grusha.png",
                description = "El ex-snowboarder profesional de sangre fría y mirada gélida."
            )
        ))
        println("✅ Base de datos de Líderes cargada con éxito para las 9 regiones.")
    }
}