import org.openrndr.extra.osc.OSC
import org.openrndr.resourceUrl
import java.io.File
import java.net.URL

class OscLoader() {
    var csv= mutableListOf<String>()
    var messages = mutableListOf<OscMessage>()

    val portIn = 57130
    val portOut = 57120
    val osc = OSC(portOut = portOut, portIn = portIn)

    init {

        File("data/osc_messages.csv").readText().split("\n").forEach {
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
            // println("send: ${it.timecode} ${it.hasBeenSend} ${it.channel} ${it.data} ${it.meta}")

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

            if(it.channel.equals("/nowhere/circle_pulse")) {
                osc.send("/nowhere/circle_pulse", it.data.toDouble())
            }

            if(it.channel.equals("/nowhere/tick")) {
                val list = it.data.split(",")
                osc.send("/nowhere/tick", list[0].toString(), list[1].toDouble())
            }
            it.hasBeenSend = true
        }
    }
}
