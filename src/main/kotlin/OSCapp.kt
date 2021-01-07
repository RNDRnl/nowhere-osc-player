import org.openrndr.application
import org.openrndr.draw.loadFont
import org.openrndr.extra.osc.OSC
import org.openrndr.ffmpeg.VideoPlayerFFMPEG
import org.openrndr.resourceUrl
import java.net.URL

class OscMessage(
    var index: Int,
    var timecode: Double,
    var channel: String,
    var data: String,
    var meta: String
) {
    var hasBeenSend = false

}

class OscLoader() {
    var csv= mutableListOf<String>()
    var messages = mutableListOf<OscMessage>()

    val portIn = 57130
    val portOut = 57120
    val osc = OSC(portOut = portOut, portIn = portIn)

    init {

        URL(resourceUrl("/osc_messages.csv")).readText().split("\n").forEach {
            csv.add(it.trim())
        }

        parse()
    }

    fun parse() {
        csv.forEachIndexed { row, s ->
            if(row > 0) {
                val list = s.split(";")
                messages.add(OscMessage(list[0].toInt(), list[1].toDouble(), list[2].toString(), list[3].toString(), list[4].toString()))
            }
        }

        messages.forEach {
            println(it.timecode)

        }
    }

    fun reset() {
        messages.forEach {
            it.hasBeenSend = false
        }
    }

    fun dispatch(seconds: Double) {

        messages.filterIndexed { index, oscMessage ->
            oscMessage.timecode < seconds
        }.filterIndexed { index, oscMessage ->
            oscMessage.hasBeenSend == false
        }.forEach {
            println("send: ${it.timecode} ${it.hasBeenSend} ${it.channel} ${it.data} ${it.meta}")

            var meta = it.meta.split(",")
            if(it.channel.equals("/nowhere/trigger")) {
                var list = it.data.split(",")
                osc.send(
                    "/nowhere/trigger",
                    list[0],
                    list[1],
                    list[2],
                    *meta.toTypedArray()
                )
            }

            if(it.channel.equals("/nowhere/collection_change")) {
                osc.send("/nowhere/collection_change", it.data.toDouble())
            }

            if(it.channel.equals("/nowhere/show_crop")) {
                osc.send("/nowhere/show_crop", it.data.toDouble())
            }

            if(it.channel.equals("/nowhere/tick")) {
                val list = it.data.split(",")
                osc.send("/nowhere/tick", list[0].toString(), list[1].toDouble())
            }
            it.hasBeenSend = true
        }
    }
}

fun main() = application {
    configure {
        width = 1920
        height = 360
    }

    program {

        val font = loadFont(resourceUrl("/default.otf"), 64.0)
        val oscLoader = OscLoader()
        drawer.fontMap = font

        val videoPlayer = VideoPlayerFFMPEG.fromFile(resourceUrl("/osc_video.mp4"))
        videoPlayer.play()

        val oldClock = clock
        val oldClockStart = clock()
        clock = { oldClock() - oldClockStart }

        var startTime = seconds

        keyboard.keyUp.listen {
            if(it.name == "r") {
                videoPlayer.restart()
                oscLoader.reset()
                startTime = seconds
            }
        }

        extend {
            videoPlayer.draw(drawer)
            drawer.text("${seconds-startTime}", 250.0, 150.0)
            oscLoader.dispatch(seconds-startTime)
        }
    }
}
