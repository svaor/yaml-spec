package org.svaor.tutorial.yaml.spec

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature.*
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.TestMethodOrder
import org.yaml.snakeyaml.TypeDescription
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.nodes.Node
import org.yaml.snakeyaml.nodes.SequenceNode
import org.yaml.snakeyaml.nodes.Tag
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.Double.Companion.NEGATIVE_INFINITY
import kotlin.Double.Companion.NaN


@TestMethodOrder(MethodOrderer.MethodName::class)
class OverviewExamplesTest {
    private val log by LoggerDelegate()

    @Test
    fun `01 Sequence of Scalars`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
- Mark McGwire
- Sammy Sosa
- Ken Griffey
"""

        val typeRef = object : TypeReference<List<String>>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(result, equalTo(listOf("Mark McGwire", "Sammy Sosa", "Ken Griffey")))
    }

    @Test
    fun `02 Mapping Scalars to Scalars`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
hr:  65    # Home runs
avg: 0.278 # Batting average
rbi: 147   # Runs Batted In
"""

        val typeRef = object : TypeReference<PlayerStatistics>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(result, equalTo(PlayerStatistics(hr = 65, avg = 0.278, rbi = 147)))
    }

    @Test
    fun `03 Mapping Scalars to Sequences`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
american:
- Boston Red Sox
- Detroit Tigers
- New York Yankees
national:
- New York Mets
- Chicago Cubs
- Atlanta Braves
"""

        val typeRef = object : TypeReference<Map<String, *>>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                mapOf(
                    "american" to listOf("Boston Red Sox", "Detroit Tigers", "New York Yankees"),
                    "national" to listOf("New York Mets", "Chicago Cubs", "Atlanta Braves"),
                )
            )
        )
    }

    @Test
    fun `04 Sequence of Mappings`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
-
  name: Mark McGwire
  hr:   65
  avg:  0.278
-
  name: Sammy Sosa
  hr:   63
  avg:  0.288
"""

        val typeRef = object : TypeReference<List<PlayerStatistics>>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                listOf(
                    PlayerStatistics(name = "Mark McGwire", hr = 65, avg = 0.278),
                    PlayerStatistics(name = "Sammy Sosa", hr = 63, avg = 0.288),
                )
            )
        )
    }

    @Test
    fun `05 Sequence of Sequences`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
- [name        , hr, avg  ]
- [Mark McGwire, 65, 0.278]
- [Sammy Sosa  , 63, 0.288]
"""

        val typeRef = object : TypeReference<List<List<*>>>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                listOf(
                    listOf("name", "hr", "avg"),
                    listOf("Mark McGwire", 65, 0.278),
                    listOf("Sammy Sosa", 63, 0.288),
                )
            )
        )
    }

    @Test
    fun `06 Mapping of Mappings`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
Mark McGwire: {hr: 65, avg: 0.278}
Sammy Sosa: {
  hr: 63,
  avg: 0.288,
}
"""

        val typeRef = object : TypeReference<Map<String, PlayerStatistics>>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                mapOf(
                    "Mark McGwire" to PlayerStatistics(hr = 65, avg = 0.278),
                    "Sammy Sosa" to PlayerStatistics(hr = 63, avg = 0.288),
                )
            )
        )
    }

    @Test
    fun `07 Two Documents in a Stream`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
# Ranking of 1998 home runs
---
- Mark McGwire
- Sammy Sosa
- Ken Griffey

# Team ranking
---
- Chicago Cubs
- St Louis Cardinals
"""

        val yamlParser = mapper.createParser(yaml)
        val typeRef = object : TypeReference<Any>() {}
        val results = mapper.readValues(yamlParser, typeRef).readAll()

        val test = testInfo.testMethod.orElseThrow().name

        log.info("$test, result: {}", results)
        assertThat(results.size, equalTo(2))
        assertThat(
            results[0], equalTo(
                listOf(
                    "Mark McGwire", "Sammy Sosa", "Ken Griffey",
                )
            )
        )
        assertThat(
            results[1], equalTo(
                listOf(
                    "Chicago Cubs", "St Louis Cardinals",
                )
            )
        )
    }

    @Test
    fun `08 Play by Play Feed from a Game`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
---
time: 20:03:20
player: Sammy Sosa
action: strike (miss)
...
---
time: 20:03:47
player: Sammy Sosa
action: grand slam
...
"""

        val yamlParser = mapper.createParser(yaml)
        val typeRef = object : TypeReference<PlayerEvent>() {}
        val results = mapper.readValues(yamlParser, typeRef).readAll()

        val test = testInfo.testMethod.orElseThrow().name

        log.info("$test, result: {}", results)
        assertThat(results.size, equalTo(2))
        assertThat(
            results[0], equalTo(
                PlayerEvent(
                    LocalTime.parse("20:03:20"),
                    "Sammy Sosa",
                    "strike (miss)",
                )
            )
        )
        assertThat(
            results[1], equalTo(
                PlayerEvent(
                    LocalTime.parse("20:03:47"),
                    "Sammy Sosa",
                    "grand slam",
                )
            )
        )
    }

    @Test
    fun `09 Single Document with Two Comments`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
---
hr: # 1998 hr ranking
- Mark McGwire
- Sammy Sosa
# 1998 rbi ranking
rbi:
- Sammy Sosa
- Ken Griffey
...
"""

        val typeRef = object : TypeReference<Map<String, List<String>>>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                mapOf(
                    "hr" to listOf("Mark McGwire", "Sammy Sosa"),
                    "rbi" to listOf("Sammy Sosa", "Ken Griffey"),
                )
            )
        )
    }

    @Test
    fun `10 Node for Sammy Sosa appears twice in this document`(testInfo: TestInfo) {
        val yamlResource = """
---
hr:
- Mark McGwire
# Following node labeled SS
- &SS Sammy Sosa
rbi:
- *SS # Subsequent occurrence
- Ken Griffey
"""

        //Jackson does not support YAML templates
        val yaml = Yaml()
        val result = yaml.loadAs(yamlResource, HrRbi::class.java)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                HrRbi(
                    hr = listOf("Mark McGwire", "Sammy Sosa"),
                    rbi = listOf("Sammy Sosa", "Ken Griffey"),
                )
            )
        )
    }

    @Test
    fun `11 Mapping between Sequences`(testInfo: TestInfo) {
        val yamlResource = """
? - Detroit Tigers
  - Chicago cubs
: - 2001-07-23

? [ New York Yankees,
    Atlanta Braves ]
: [ 2001-07-02, 2001-08-12,
    2001-08-14 ]
"""

        val yaml = Yaml()
        val result = yaml.loadAs(yamlResource, Map::class.java)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                mapOf(
                    listOf("Detroit Tigers", "Chicago cubs") to listOf(
                        Date.from(LocalDate.of(2001, 7, 23).atStartOfDay(ZoneId.of("UTC")).toInstant())
                    ),
                    listOf("New York Yankees", "Atlanta Braves") to listOf(
                        Date.from(LocalDate.of(2001, 7, 2).atStartOfDay(ZoneId.of("UTC")).toInstant()),
                        Date.from(LocalDate.of(2001, 8, 12).atStartOfDay(ZoneId.of("UTC")).toInstant()),
                        Date.from(LocalDate.of(2001, 8, 14).atStartOfDay(ZoneId.of("UTC")).toInstant()),
                    ),
                )
            )
        )
    }

    @Test
    fun `12 (SnakeYaml) Compact Nested Mapping`(testInfo: TestInfo) {
        val yamlResource = """
---
# Products purchased
- item    : Super Hoop
  quantity: 1
- item    : Basketball
  quantity: 4
- item    : Big Shoes
  quantity: 1
"""

        class ListConstructor<T>(private val clazz: Class<T>) : Constructor() {
            var root = true

            override fun constructObject(node: Node): Any {
                if (node is SequenceNode && root) {
                    node.setListType(clazz)
                    root = false
                }
                return super.constructObject(node)
            }
        }

        val yaml = Yaml(ListConstructor(ItemQuantity::class.java))
        val result = yaml.loadAs(yamlResource, List::class.java)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                listOf(
                    ItemQuantity("Super Hoop", 1),
                    ItemQuantity("Basketball", 4),
                    ItemQuantity("Big Shoes", 1),
                )
            )
        )
    }

    @Test
    fun `12 (Jackson) Compact Nested Mapping`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
---
# Products purchased
- item    : Super Hoop
  quantity: 1
- item    : Basketball
  quantity: 4
- item    : Big Shoes
  quantity: 1
"""

        val typeRef = object : TypeReference<List<ItemQuantity>>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                listOf(
                    ItemQuantity("Super Hoop", 1),
                    ItemQuantity("Basketball", 4),
                    ItemQuantity("Big Shoes", 1),
                )
            )
        )
    }

    @Test
    fun `13 In literals, newlines are preserved`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
# ASCII Art
--- |
  \//||\/||
  // ||  ||__
"""

        val result = mapper.readValue(yaml, String::class.java)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                """|\//||\/||
                   |// ||  ||__
                   |""".trimMargin()
            )
        )
    }

    @Test
    fun `14 In the folded scalars, newlines become spaces`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
--- >
  Mark McGwire's
  year was crippled
  by a knee injury.
"""

        val result = mapper.readValue(yaml, String::class.java)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(result, equalTo("Mark McGwire's year was crippled by a knee injury.\n"))
    }

    @Test
    fun `15 Folded newlines are preserved for more indented and blank lines`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
--- >
 Sammy Sosa completed another
 fine season with great stats.

   63 Home Runs
   0.288 Batting Average

 What a year!
"""

        val result = mapper.readValue(yaml, String::class.java)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                """|Sammy Sosa completed another fine season with great stats.
                                      |
                                      |  63 Home Runs
                                      |  0.288 Batting Average
                                      |
                                      |What a year!
                                      |""".trimMargin()
            )
        )
    }

    @Test
    fun `16 Indentation determines scope`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
name: Mark McGwire
accomplishment: >
  Mark set a major league
  home run record in 1998.
stats: |
  65 Home Runs
  0.278 Batting Average
"""

        val typeRef = object : TypeReference<Map<String, String>>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                mapOf(
                    "name" to "Mark McGwire",
                    "accomplishment" to "Mark set a major league home run record in 1998.\n",
                    "stats" to "65 Home Runs\n0.278 Batting Average\n",
                )
            )
        )
    }

    @Test
    fun `17 Quoted Scalars`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
unicode: "Sosa did fine.\u263A"
control: "\b1998\t1999\t2000\n"
hex esc: "\x0d\x0a is \r\n"

single: '"Howdy!" he cried.'
quoted: ' # Not a ''comment''.'
tie-fighter: '|\-*-/|'
"""

        val typeRef = object : TypeReference<Map<String, String>>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                mapOf(
                    "unicode" to "Sosa did fine.☺",
                    "control" to "\b1998\t1999\t2000\n",
                    "hex esc" to "\r\n is \r\n",
                    "single" to "\"Howdy!\" he cried.",
                    "quoted" to " # Not a 'comment'.",
                    "tie-fighter" to "|\\-*-/|",
                )
            )
        )
    }

    @Test
    fun `18 Multi-line Flow Scalars`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
plain:
  This unquoted scalar
  spans many lines.

quoted: "So does this
  quoted scalar.\n"
"""

        val typeRef = object : TypeReference<Map<String, String>>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                mapOf(
                    "plain" to "This unquoted scalar spans many lines.",
                    "quoted" to "So does this quoted scalar.\n",
                )
            )
        )
    }

    @Test
    fun `19 (Jackson) Integers`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
canonical: 12345
decimal: +12345
#octal: 0o14
hexadecimal: 0xC
"""

        val typeRef = object : TypeReference<Map<String, Int>>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                mapOf(
                    "canonical" to 12345,
                    "decimal" to 12345,
//                    "octal" to 12,
                    "hexadecimal" to 12,
                )
            )
        )
    }

    @Test
    fun `19 (SnakeYaml) Integers`(testInfo: TestInfo) {
        val yamlResource = """
canonical: 12345
decimal: +12345
octal: 0o14
hexadecimal: 0xC
"""

        val yaml = Yaml()
        val result = yaml.loadAs(yamlResource, Map::class.java)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                mapOf(
                    "canonical" to 12345,
                    "decimal" to 12345,
                    "octal" to "0o14",
                    "hexadecimal" to 12,
                )
            )
        )
    }

    @Test
    fun `20 (Jackson) Floating Point`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        //-.inf and .nan is not supported by Jackson
        val yaml = """
canonical: 1.23015e+3
exponential: 12.3015e+02
fixed: 1230.15
#negative infinity: -.inf
#not a number: .nan
"""

        val typeRef = object : TypeReference<Map<String, Double>>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                mapOf(
                    "canonical" to 1230.15,
                    "exponential" to 1230.15,
                    "fixed" to 1230.15,
//                    "negative infinity" to Double.NEGATIVE_INFINITY,
//                    "not a number" to Double.NaN,
                )
            )
        )
    }

    @Test
    fun `20 (SnakeYaml) Floating Point`(testInfo: TestInfo) {
        val yamlResource = """
canonical: 1.23015e+3
exponential: 12.3015e+02
fixed: 1230.15
negative infinity: -.inf
not a number: .nan
"""

        val yaml = Yaml()
        val result = yaml.loadAs(yamlResource, Map::class.java)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                mapOf(
                    "canonical" to 1230.15,
                    "exponential" to 1230.15,
                    "fixed" to 1230.15,
                    "negative infinity" to NEGATIVE_INFINITY,
                    "not a number" to NaN,
                )
            )
        )
    }

    @Test
    fun `21 Miscellaneous`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
null:
booleans: [ true, false ]
string: '012345'
"""

        val typeRef = object : TypeReference<Map<String, *>>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(
            result, equalTo(
                mapOf(
                    "null" to null,
                    "booleans" to listOf(true, false),
                    "string" to "012345",
                )
            )
        )
    }

    @Test
    fun `22 (Jackson) Timestamps`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        //custom formats needed for mapping
        val yaml1 = """
#canonical: 2001-12-15T02:59:43.1Z
#iso8601: 2001-12-14t21:59:43.10-05:00
#spaced: 2001-12-14 21:59:43.10 -5
date: 2002-12-14
"""

        val typeRef1 = object : TypeReference<Map<String, LocalDate>>() {}
        val result1 = mapper.readValue(yaml1, typeRef1)

        val test1 = testInfo.testMethod.orElseThrow().name
        log.info("$test1, result: {}", result1)
        log.info("$test1, result: {}", result1["date"]?.javaClass)
        assertThat(
            result1, equalTo(
                mapOf(
                    "date" to LocalDate.of(2002, 12, 14),
                )
            )
        )

        val yaml2 = """
canonical: 2001-12-15T02:59:43.1Z
iso8601: 2001-12-14t21:59:43.10-05:00
spaced: 2001-12-14 21:59:43.10 -05
date: 2002-12-14
"""

        val typeRef2 = object : TypeReference<Dates>() {}
        val result2 = mapper.readValue(yaml2, typeRef2)

        val test2 = testInfo.testMethod.orElseThrow().name
        log.info("$test2, result: {}", result2)
        assertThat(
            result2, equalTo(
                Dates(
                    ZonedDateTime.of(2001, 12, 15, 2, 59, 43, 100000000, ZoneId.of("+1")).toLocalDateTime(),
                    ZonedDateTime.of(2001, 12, 14, 21, 59, 43, 100000000, ZoneId.of("-5")).toLocalDateTime(),
                    ZonedDateTime.of(2001, 12, 14, 21, 59, 43, 100000000, ZoneId.of("-5")).toLocalDateTime(),
                    LocalDate.of(2002, 12, 14).atStartOfDay(ZoneId.of("UTC")).toLocalDate()
                )
            )
        )
    }

    @Test
    fun `22 (SnakeYaml) Timestamps`(testInfo: TestInfo) {
        val yamlResource = """
canonical: 2001-12-15T02:59:43.1Z
iso8601: 2001-12-14t21:59:43.10-05:00
spaced: 2001-12-14 21:59:43.10 -5
date: 2002-12-14
"""

        val yaml = Yaml()
        val result = yaml.loadAs(yamlResource, Map::class.java)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        log.info("$test, canonical: {}", result["canonical"]?.javaClass)
        log.info("$test, iso8601: {}", result["iso8601"]?.javaClass)
        log.info("$test, spaced: {}", result["spaced"]?.javaClass)
        log.info("$test, date: {}", result["date"]?.javaClass)
        assertThat(
            result["canonical"],
            equalTo(Date.from(ZonedDateTime.of(2001, 12, 15, 2, 59, 43, 100000000, ZoneId.of("UTC")).toInstant()))
        ) //2001-12-15T02:59:43.1Z
        assertThat(result["iso8601"], equalTo(Date.from(ZonedDateTime.of(2001, 12, 14, 21, 59, 43, 100000000, ZoneId.of("-5")).toInstant())))
        assertThat(result["spaced"], equalTo(Date.from(ZonedDateTime.of(2001, 12, 14, 21, 59, 43, 100000000, ZoneId.of("-5")).toInstant())))
        assertThat(result["date"], equalTo(Date.from(LocalDate.of(2002, 12, 14).atStartOfDay(ZoneId.of("UTC")).toInstant())))
    }

    @Test
    fun `23 (Jackson) Various Explicit Tags`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        //base64 in one line
        val yaml = """
---
not-date: !!str 2002-04-28

picture: !!binary |
 R0lGODlhDAAMAIQAAP//9/X17unp5WZmZgAAAOfn515eXvPz7Y6OjuDg4J+fn5OTk6enp56enmleECcgggoBADs=

application specific tag: !something |
 The semantics of the tag
 above may be different for
 different documents.
"""

        val typeRef = object : TypeReference<Map<String, *>>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test1 = testInfo.testMethod.orElseThrow().name
        log.info("$test1, result: {}", result)

        assertThat(result["not-date"], equalTo("2002-04-28"))
        assertThat(
            result["picture"],
            equalTo(Base64.getDecoder().decode("R0lGODlhDAAMAIQAAP//9/X17unp5WZmZgAAAOfn515eXvPz7Y6OjuDg4J+fn5OTk6enp56enmleECcgggoBADs="))
        )
        assertThat(
            result["application specific tag"], equalTo(
                """|The semantics of the tag
                                                 |above may be different for
                                                 |different documents.
                                                 |""".trimMargin()
            )
        )
    }

    @Test
    fun `23 (SnakeYaml) Various Explicit Tags`(testInfo: TestInfo) {
        //exception=Invalid tag: !something
        val yamlResource = """
---
not-date: !!str 2002-04-28

picture: !!binary |
 R0lGODlhDAAMAIQAAP//9/X
 17unp5WZmZgAAAOfn515eXv
 Pz7Y6OjuDg4J+fn5OTk6enp
 56enmleECcgggoBADs=

#application specific tag: !something |
# The semantics of the tag
# above may be different for
# different documents.
"""

        val yaml = Yaml()
        val result = yaml.loadAs(yamlResource, Map::class.java)

        val test1 = testInfo.testMethod.orElseThrow().name
        log.info("$test1, result: {}", result)

        assertThat(result["not-date"], equalTo("2002-04-28"))
        assertThat(
            result["picture"],
            equalTo(Base64.getDecoder().decode("R0lGODlhDAAMAIQAAP//9/X17unp5WZmZgAAAOfn515eXvPz7Y6OjuDg4J+fn5OTk6enp56enmleECcgggoBADs="))
        )
    }

    @Test
    fun `24 Global Tags`(testInfo: TestInfo) {
        val yamlResource = """
%TAG ! tag:clarkevans.com,2002:
--- !shape
  # Use the ! handle for presenting
  # tag:clarkevans.com,2002:circle
- !circle
  center: &ORIGIN {x: 73, y: 129}
  radius: 7
- !line
  start: *ORIGIN
  finish: { x: 89, y: 102 }
- !label
  start: *ORIGIN
  color: 0xFFEEBB
  text: Pretty vector drawing.
"""

        val constr = Constructor()
        constr.addTypeDescription(TypeDescription(Shape::class.java, Tag("tag:clarkevans.com,2002")))
        constr.addTypeDescription(TypeDescription(Circle::class.java, Tag("tag:clarkevans.com,2002:circle")))
        constr.addTypeDescription(TypeDescription(Line::class.java, Tag("tag:clarkevans.com,2002:line")))
        constr.addTypeDescription(TypeDescription(Label::class.java, Tag("tag:clarkevans.com,2002:label")))
        val yaml = Yaml(constr)
        val result = yaml.loadAs(yamlResource, List::class.java)

        val test = testInfo.testMethod.orElseThrow().name
        log.info("$test, result: {}", result)
        assertThat(result[0], equalTo(Circle(Coordinate(73, 129), 7)))
        assertThat(result[1], equalTo(Line(Coordinate(73, 129), Coordinate(89, 102))))
        assertThat(result[2], equalTo(Label(Coordinate(73, 129), 0xFFEEBB, "Pretty vector drawing.")))
    }

    data class Shape(var circle: Circle = Circle(), var line: Line = Line(), var label: Label = Label())
    data class Circle(var center: Coordinate = Coordinate(), var radius: Int = 0)
    data class Line(var start: Coordinate = Coordinate(), var finish: Coordinate = Coordinate())
    data class Label(var start: Coordinate = Coordinate(), var color: Int = 0, var text: String = "")
    data class Coordinate(var x: Int = 0, var y: Int = 0)

    @Test
    fun `25 Unordered Sets`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
# Sets are represented as a
# Mapping where each key is
# associated with a null value
--- !!set
? Mark McGwire
? Sammy Sosa
? Ken Griffey
"""

        val typeRef = object : TypeReference<Map<String, *>>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test1 = testInfo.testMethod.orElseThrow().name
        log.info("$test1, result: {}", result)

        assertThat(result, hasEntry("Mark McGwire", null))
        assertThat(result, hasEntry("Sammy Sosa", null))
        assertThat(result, hasEntry("Ken Griffey", null))
    }

    @Test
    fun `26 Ordered Mappings`(testInfo: TestInfo) {
        val mapper = prepareMapper()

        val yaml = """
# Ordered maps are represented as
# A sequence of mappings, with
# each mapping having one key
--- !!omap
- Mark McGwire: 65
- Sammy Sosa: 63
- Ken Griffey: 58
"""

        val typeRef = object : TypeReference<List<Map<String, Int>>>() {}
        val result = mapper.readValue(yaml, typeRef)

        val test1 = testInfo.testMethod.orElseThrow().name
        log.info("$test1, result: {}", result)

        assertThat(result[0], equalTo(mapOf("Mark McGwire" to 65)))
        assertThat(result[1], equalTo(mapOf("Sammy Sosa" to 63)))
        assertThat(result[2], equalTo(mapOf("Ken Griffey" to 58)))
    }

    private fun prepareMapper(): ObjectMapper {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.findAndRegisterModules()
        mapper.registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(NullToEmptyCollection, false)
                .configure(NullToEmptyMap, false)
                .configure(NullIsSameAsDefault, false)
                .configure(SingletonSupport, false)
                .configure(StrictNullChecks, false)
                .build()
        )

        return mapper
    }
}
