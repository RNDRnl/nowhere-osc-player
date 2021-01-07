import org.openrndr.application
import org.openrndr.draw.loadFont
import org.openrndr.ffmpeg.VideoPlayerFFMPEG
import org.openrndr.resourceUrl


fun main() = application {
    configure {
        width = 1920
        height = 360
    }

    program {

        val font = loadFont(resourceUrl("/default.otf"), 64.0)
        val oscLoader = OscLoader()
        drawer.fontMap = font

        val videoPlayer = VideoPlayerFFMPEG.fromFile("data/osc_video.mp4")
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
