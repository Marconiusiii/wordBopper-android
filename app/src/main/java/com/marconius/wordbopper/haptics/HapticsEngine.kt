package com.marconius.wordbopper.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.accessibility.AccessibilityManager
import kotlin.math.roundToInt

/**
 * Android counterpart to the iOS HapticsEngine. Reproduces the same set of gameplay
 * haptics as closely as Android's vibration affordances allow.
 *
 * iOS uses UIImpactFeedbackGenerator styles (light/soft/medium/rigid/heavy) plus a 0...1
 * intensity. Android exposes a single linear amplitude (1..255) and durations, so each iOS
 * "style" is mapped to a base duration and amplitude weighting, then scaled by the iOS
 * intensity. Multi-pulse iOS sequences (which iOS schedules with asyncAfter delays) are
 * composed here into a single VibrationEffect waveform so the timing stays tight and is not
 * subject to coroutine scheduling jitter.
 */
class HapticsEngine(context: Context) {

    var isEnabled = true

    private val appContext = context.applicationContext

    private val vibrator: Vibrator? = run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Vibrator::class.java)
        }
    }

    private val accessibilityManager =
        appContext.getSystemService(AccessibilityManager::class.java)

    private val hasAmplitudeControl: Boolean =
        vibrator?.hasVibrator() == true && vibrator.hasAmplitudeControl()

    // iOS feedback styles approximated as (durationMs, amplitudeWeight at intensity 1.0).
    private enum class Style(val durationMs: Long, val maxAmplitude: Int) {
        SOFT(28, 150),
        LIGHT(20, 170),
        MEDIUM(30, 210),
        RIGID(24, 235),
        HEAVY(40, 255)
    }

    private data class Pulse(val style: Style, val intensity: Double, val delayMs: Long)

    // MARK: - Interaction haptics (suppressed under TalkBack, matching iOS VoiceOver behavior)

    fun selectLetter() {
        if (!shouldPlayInteractionHaptic()) return
        // iOS UISelectionFeedbackGenerator().selectionChanged() — a crisp, light tick.
        playTick()
    }

    fun deselectLetter() {
        if (!shouldPlayInteractionHaptic()) return
        play(listOf(Pulse(Style.LIGHT, 0.45, 0)))
    }

    // MARK: - Game event haptics

    fun clearLetters() {
        if (!isEnabled) return
        play(
            listOf(
                Pulse(Style.MEDIUM, 0.65, 0),
                Pulse(Style.LIGHT, 0.45, 60)
            )
        )
    }

    fun invalidWord() {
        if (!isEnabled) return
        play(
            listOf(
                Pulse(Style.MEDIUM, 0.7, 0),
                Pulse(Style.RIGID, 0.7, 50)
            )
        )
    }

    fun wordScored(wordLength: Int) {
        if (!isEnabled) return
        val noteCount = if (wordLength >= 7) 7 else if (wordLength >= 5) 5 else 3
        val spacing = if (wordLength >= 7) 55.0 else if (wordLength >= 5) 65.0 else 75.0
        val finishTime = spacing * noteCount + 520.0

        val pulses = mutableListOf<Pulse>()
        pulses.add(Pulse(Style.MEDIUM, if (wordLength >= 7) 0.82 else 0.64, 0))
        for (index in 0 until noteCount) {
            val intensity = (0.38 + index * 0.07).coerceAtMost(0.82)
            pulses.add(Pulse(Style.LIGHT, intensity, (index * spacing).roundToLong()))
        }
        pulses.add(Pulse(Style.SOFT, 0.42, finishTime.roundToLong()))
        pulses.add(Pulse(Style.SOFT, 0.28, (finishTime + 260.0).roundToLong()))
        play(pulses)
    }

    fun chainWord() {
        if (!isEnabled) return
        val pulses = mutableListOf<Pulse>()
        for (index in 0 until 4) {
            pulses.add(Pulse(Style.LIGHT, 0.36 + index * 0.08, (index * 45).toLong()))
        }
        pulses.add(Pulse(Style.MEDIUM, 0.82, 200))
        pulses.add(Pulse(Style.SOFT, 0.34, 460))
        play(pulses)
    }

    fun powerUpActivated() {
        if (!isEnabled) return
        play(
            listOf(
                Pulse(Style.LIGHT, 0.35, 0),
                Pulse(Style.LIGHT, 0.48, 70),
                Pulse(Style.MEDIUM, 0.64, 140),
                Pulse(Style.HEAVY, 0.88, 240),
                Pulse(Style.SOFT, 0.36, 420)
            )
        )
    }

    fun powerUpScored() {
        if (!isEnabled) return
        val glissandoTimes = listOf(20L, 85L, 150L, 215L, 280L, 340L)
        val pulses = mutableListOf<Pulse>()
        glissandoTimes.forEachIndexed { index, time ->
            pulses.add(Pulse(Style.LIGHT, 0.36 + index * 0.08, time))
        }
        pulses.add(Pulse(Style.MEDIUM, 0.62, 255))
        pulses.add(Pulse(Style.MEDIUM, 0.72, 295))
        pulses.add(Pulse(Style.MEDIUM, 0.82, 335))
        pulses.add(Pulse(Style.HEAVY, 1.0, 390))
        pulses.add(Pulse(Style.SOFT, 0.62, 680))
        pulses.add(Pulse(Style.SOFT, 0.42, 950))
        pulses.add(Pulse(Style.SOFT, 0.26, 1200))
        play(pulses)
    }

    fun roundStarted() {
        if (!isEnabled) return
        play(listOf(Pulse(Style.LIGHT, 0.5, 0)))
    }

    fun roundEnded() {
        if (!isEnabled) return
        val pulses = mutableListOf<Pulse>()
        for (index in 0 until 7) {
            val style = if (index == 6) Style.HEAVY else Style.LIGHT
            val intensity = if (index == 6) 0.95 else (0.36 + index * 0.07).coerceAtMost(0.76)
            pulses.add(Pulse(style, intensity, (index * 85).toLong()))
        }
        pulses.add(Pulse(Style.SOFT, 0.42, 920))
        pulses.add(Pulse(Style.SOFT, 0.28, 1200))
        play(pulses)
    }

    // MARK: - Playback

    private fun shouldPlayInteractionHaptic(): Boolean =
        isEnabled && accessibilityManager?.isTouchExplorationEnabled != true

    private fun playTick() {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            play(listOf(Pulse(Style.LIGHT, 0.4, 0)))
        }
    }

    /**
     * Composes the pulses into a single waveform. iOS schedules each pulse independently with a
     * delay from "now"; here the delays are absolute offsets from the start of the sequence, so
     * they are converted into the gap/on/gap/on... timing array a waveform expects.
     */
    private fun play(pulses: List<Pulse>) {
        val v = vibrator ?: return
        if (!v.hasVibrator() || pulses.isEmpty()) return

        val ordered = pulses.sortedBy { it.delayMs }
        val timings = mutableListOf<Long>()
        val amplitudes = mutableListOf<Int>()

        var cursor = 0L
        for (pulse in ordered) {
            val gap = (pulse.delayMs - cursor).coerceAtLeast(0)
            // Leading gap (no vibration).
            timings.add(gap)
            amplitudes.add(0)
            // The pulse itself.
            timings.add(pulse.style.durationMs)
            amplitudes.add(amplitudeFor(pulse.style, pulse.intensity))
            cursor = pulse.delayMs + pulse.style.durationMs
        }

        if (hasAmplitudeControl) {
            v.vibrate(VibrationEffect.createWaveform(timings.toLongArray(), amplitudes.toIntArray(), -1))
        } else {
            // No amplitude control: fall back to an on/off pattern. createWaveform without
            // amplitudes treats every other entry as "on", which already matches our gap/pulse
            // ordering (index 0 is the leading gap = off).
            v.vibrate(VibrationEffect.createWaveform(timings.toLongArray(), -1))
        }
    }

    private fun amplitudeFor(style: Style, intensity: Double): Int {
        val scaled = (style.maxAmplitude * intensity.coerceIn(0.0, 1.0)).roundToInt()
        return scaled.coerceIn(1, 255)
    }

    private fun Double.roundToLong(): Long = this.roundToInt().toLong()

    fun cancel() {
        vibrator?.cancel()
    }
}
