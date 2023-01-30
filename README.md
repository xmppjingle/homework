# HomeWork Library

Home and Work automatic classification library. 
This library is used to parse and calculate scores based on postcode data and timetable images. It has the ability to read and parse CSV files, merge records by postcode, calculate total scores by postcode, and create a `HomeWorkProfile` data class based on the parsed data.

## Installation

To use the library, add the following to your Gradle or Maven dependencies:

```
dependencies {
implementation 'com.github.xmppjingle:homework:0.0.3'
}
```

## Usage

Here is an example of how to use the library to calculate total scores by postcode and create a `HomeWorkProfile` data class:

```kotlin
    fun `simple calculateTotalScoreByPostCode should return correct scores`() {
        File("src/test/resources/timetables").walk().filter { it.isFile }.forEach {

            val input = FileInputStream(it)
            val records = ScoreParserUtils.readAndParseFile(input)
            val timetable = pngToTimetable(ImageIO.read(File("defaultTimetable.png")) as BufferedImage)

            val result = getHomeWorkProfile(calculateTotalScoreByPostCode(records, timetable))

            println("${(result)} - ${it.name}")

            val expected = parseFilename(it.name)

            assertEquals(expected, (result))

        }

    }
```



