package ru.mipt.npm.muon.monitor.sim

import org.apache.commons.math3.geometry.euclidean.threed.Line
import org.apache.commons.math3.random.RandomGenerator
import ru.mipt.npm.muon.monitor.Event
import ru.mipt.npm.muon.monitor.Monitor.PIXEL_XY_SIZE


/**
 * Simulate single track and returns corresponding event
 */
fun TrackGenerator.simulateOne(): Event {
    val track = generate()
    return buildEventByTrack(track)
}

interface TrackGenerator {
    val rnd: RandomGenerator
    fun generate(): Line
}

/**
 * A uniform generator with track bases distributed in square in central plane, uniform phi and cos theta
 */
class UniformTrackGenerator(override val rnd: RandomGenerator, val maxX: Double = 4 * PIXEL_XY_SIZE, val maxY: Double = 4 * PIXEL_XY_SIZE) :
    TrackGenerator {
    override fun generate(): Line {
        val x = (1 - rnd.nextDouble() * 2.0) * maxX
        val y = (1 - rnd.nextDouble() * 2.0) * maxY
        val phi = (1 - rnd.nextDouble() * 2.0) * Math.PI
        val theta = Math.PI / 2 - Math.acos(rnd.nextDouble())
        return makeTrack(x, y, theta, phi)
    }
}

class FixedAngleGenerator(
    override val rnd: RandomGenerator,
    val phi: Double, val theta: Double,
    val maxX: Double = 4 * PIXEL_XY_SIZE,
    val maxY: Double = 4 * PIXEL_XY_SIZE
) : TrackGenerator {
    override fun generate(): Line {
        val x = (1 - rnd.nextDouble() * 2.0) * maxX
        val y = (1 - rnd.nextDouble() * 2.0) * maxY
        return makeTrack(x, y, theta, phi)
    }
}

/**
 * Generating surface distribution using accept-reject method
 */
class Cos2TrackGenerator(
    override val rnd: RandomGenerator,
    val power: Double = 2.0,
    val maxX: Double = 4 * PIXEL_XY_SIZE,
    val maxY: Double = 4 * PIXEL_XY_SIZE
) :
    TrackGenerator {
    override fun generate(): Line {
        val x = (1 - rnd.nextDouble() * 2.0) * maxX
        val y = (1 - rnd.nextDouble() * 2.0) * maxY
        val phi = (1 - rnd.nextDouble() * 2.0) * Math.PI


        for (i in 0..500) {
            val thetaCandidate = Math.acos(rnd.nextDouble())
            val u = rnd.nextDouble()
            val sin = Math.sin(thetaCandidate)
            if (u < Math.pow(sin, power) / sin) {
                return makeTrack(x, y, thetaCandidate, phi)
            }
        }
        throw RuntimeException("Failed to generate theta from distribution")
    }
}