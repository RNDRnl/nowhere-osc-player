class OscMessage(
    var index: Int,
    var timecode: Double,
    var channel: String,
    var data: String,
    var meta: String
) {
    var hasBeenSend = false

}