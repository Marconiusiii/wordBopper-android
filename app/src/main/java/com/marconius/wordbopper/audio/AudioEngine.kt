package com.marconius.wordbopper.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tanh

class AudioEngine(private val scope: CoroutineScope) {

    private val sampleRate = 44100
    private var selectNoteIndex = 0
    private var powerUpJob: Job? = null
    private val activeTracks = CopyOnWriteArrayList<AudioTrack>()

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val audioFormat = AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
        .setSampleRate(sampleRate)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .build()

    fun resetSelectSound() { selectNoteIndex = 0 }
    fun stepSelectSoundBack() { selectNoteIndex = max(0, selectNoteIndex - 1) }

    fun playSelectSound() {
        val selectNotes = listOf(261.63, 293.66, 329.63, 349.23, 392.00, 440.00, 493.88,
            523.25, 587.33, 659.25, 698.46, 783.99, 880.00, 987.77, 1046.50)
        val step = selectNoteIndex
        selectNoteIndex += 1
        val freq = selectNotes[min(step, selectNotes.size - 1)]
        val duration = if (step >= 3) 0.64 else 0.44
        val ctx = SynthContext(duration, sampleRate)
        for ((mult, amp) in listOf(Pair(1.0, 0.58), Pair(2.0, 0.2), Pair(3.0, 0.08))) {
            ctx.addOsc(OscType.SINE, freq * mult, 0.0, 0.006, amp * 0.58, 0.38, 0.35, 0.05)
        }
        ctx.addNoise(0.0, 0.01, 0.18, highpass = true)
        if (step >= 3) addSparkle(ctx, step, 1.0)
        play(ctx.toFloatArray())
    }

    fun playDeselectSound() {
        val ctx = SynthContext(0.28, sampleRate)
        ctx.addOscWithFreqSlide(523.25, 392.0, 0.0, 0.18, 0.11)
        ctx.addOsc(OscType.SINE, 261.63, 0.035, 0.01, 0.07, 0.22, 0.35, 0.06)
        ctx.addOsc(OscType.TRIANGLE, 784.0, 0.01, 0.004, 0.025, 0.12,
            filter = FilterSpec(FilterKind.LOWPASS, 1800.0, 0.7))
        play(ctx.toFloatArray())
    }

    fun playWordSound(wordLength: Int) {
        val baseNotes = listOf(261.63, 329.63, 392.00, 493.88, 523.25, 659.25, 783.99)
        val noteCount = if (wordLength >= 7) 7 else if (wordLength >= 5) 5 else 3
        val masterVol = if (wordLength >= 7) 1.0 else if (wordLength >= 5) 0.82 else 0.65
        val spacing = if (wordLength >= 7) 0.055 else if (wordLength >= 5) 0.065 else 0.075
        val notes = baseNotes.take(noteCount)
        val ctx = SynthContext(spacing * noteCount + 0.9, sampleRate)
        for ((i, freq) in notes.withIndex()) {
            val nd = i * spacing
            ctx.addOsc(OscType.SINE, freq, nd, 0.012, 0.38 * masterVol, 0.85, 0.45, 0.1)
            ctx.addOsc(OscType.TRIANGLE, freq * 2, nd, 0.012, 0.12 * masterVol, 0.85, 0.45, 0.1)
            ctx.addOsc(OscType.SINE, freq * 0.5, nd, 0.012, 0.18 * masterVol, 0.85, 0.45, 0.1)
        }
        val subFreq = if (wordLength >= 5) 130.0 else 110.0
        val subVol = if (wordLength >= 7) 0.34 else if (wordLength >= 5) 0.26 else 0.18
        ctx.addOscWithFreqSlide(subFreq, subFreq / 2, 0.0, 0.22, subVol)
        if (wordLength >= 5) {
            ctx.addNoise(0.0, 0.045, if (wordLength >= 7) 0.4 else 0.22, highpass = false, bandpass = true)
        }
        play(ctx.toFloatArray())
    }

    fun playInvalidSound() {
        val ctx = SynthContext(0.3, sampleRate)
        ctx.addOscWithFreqSlide(280.0, 80.0, 0.0, 0.25, 0.34)
        ctx.addNoise(0.0, 0.07, 0.32, highpass = false)
        play(ctx.toFloatArray())
    }

    fun playBonusSound() {
        val ctx = SynthContext(0.45, sampleRate)
        for ((freq, startTime) in listOf(Pair(784.0, 0.0), Pair(1046.5, 0.14))) {
            ctx.addOsc(OscType.SINE, freq, startTime, 0.01, 0.42, 0.28)
            ctx.addOsc(OscType.TRIANGLE, freq * 2, startTime, 0.001, 0.12, 0.2)
        }
        play(ctx.toFloatArray())
    }

    fun playConnectedWordSound(wordLength: Int) {
        val noteCount = if (wordLength >= 7) 8 else if (wordLength >= 5) 6 else 4
        val shimmerVol = if (wordLength >= 7) 0.26 else if (wordLength >= 5) 0.2 else 0.14
        val allNotes = listOf(1046.5, 1318.51, 1567.98, 2093.0, 2637.02, 3135.96, 4186.01, 5274.04)
        val ctx = SynthContext(noteCount * 0.045 + 0.55, sampleRate)
        for ((i, freq) in allNotes.take(noteCount).withIndex()) {
            val type = if (i % 2 == 0) OscType.SINE else OscType.TRIANGLE
            ctx.addOsc(type, freq, i * 0.045, 0.01, shimmerVol, 0.5,
                filter = FilterSpec(FilterKind.BANDPASS, freq, 8.0))
        }
        play(ctx.toFloatArray())
    }

    fun playChainStreakSound(streak: Int) {
        val fifthRoots = listOf(523.25, 587.33, 659.25)
        val root = fifthRoots[min(streak - 1, fifthRoots.size - 1)]
        val ctx = SynthContext(0.46, sampleRate)
        ctx.addOsc(OscType.SINE, root, 0.0, 0.012, 0.1, 0.42)
        ctx.addOsc(OscType.TRIANGLE, root * 1.5, 0.0, 0.012, 0.08, 0.42)
        play(ctx.toFloatArray())
    }

    fun playChainMultiplierScoreSound(wordLength: Int) {
        val masterVol = if (wordLength >= 7) 1.0 else if (wordLength >= 5) 0.88 else 0.76
        val ctx = SynthContext(1.36, sampleRate)
        val glissStart = if (wordLength >= 7) 987.77 else 880.0
        val glissEnd = if (wordLength >= 7) 3951.07 else 3135.96
        ctx.addOscWithFreqSlide(glissStart, glissEnd, 0.02, 0.32, 0.18 * masterVol)
        ctx.addOscWithFreqSlide(glissStart * 1.5, glissEnd * 1.25, 0.055, 0.28, 0.08 * masterVol)
        val sparkleNotes = listOf(1318.51, 1567.98, 2093.0, 2637.02, 3135.96, 4186.01)
        for ((i, freq) in sparkleNotes.withIndex()) {
            val amp = (if (i == sparkleNotes.size - 1) 0.13 else 0.075) * masterVol
            ctx.addOsc(if (i % 2 == 0) OscType.SINE else OscType.TRIANGLE, freq, 0.045 + i * 0.038, 0.006, amp, 0.34,
                filter = FilterSpec(FilterKind.BANDPASS, freq, 9.0))
        }
        for ((i, freq) in listOf(783.99, 1046.5, 1318.51).withIndex()) {
            ctx.addOsc(OscType.SINE, freq, 0.255 + i * 0.04, 0.004, 0.105 * masterVol, 0.26,
                filter = FilterSpec(FilterKind.BANDPASS, freq, 8.0))
        }
        val finishNotes = listOf(261.63, 392.0, 523.25, 659.25, 1046.5, 1318.51, 1567.98, 2093.0)
        for (freq in finishNotes) {
            ctx.addOscWithVibrato(OscType.SINE, freq, 0.39, 0.014, 0.115 * masterVol, 0.92,
                5.2, 7.0, 0.12, 0.42, 0.14)
            ctx.addOscWithVibrato(OscType.TRIANGLE, freq * 2, 0.39, 0.008, 0.026 * masterVol, 0.78,
                5.2, 4.0, 0.14, 0.28, 0.12)
        }
        ctx.addNoise(0.03, 0.18, 0.16 * masterVol, highpass = true)
        ctx.addNoise(0.39, 0.09, 0.09 * masterVol, highpass = false, bandpass = true)
        play(ctx.toFloatArray())
    }

    fun playRoundStartSound() {
        val chordNotes = listOf(261.63, 329.63, 392.00, 523.25, 659.25, 783.99)
        val shapes = listOf(intArrayOf(0,1,2,3), intArrayOf(2,1,3,0),
            intArrayOf(1,3,2,4), intArrayOf(3,2,4,5), intArrayOf(4,2,3,1))
        val notes = shapes.random().map { chordNotes[it] }
        val ctx = SynthContext(notes.size * 0.07 + 0.5, sampleRate)
        for ((i, freq) in notes.withIndex()) {
            ctx.addOsc(OscType.SINE, freq, i * 0.07, 0.012, 0.27, 0.46)
            ctx.addOsc(OscType.TRIANGLE, freq * 2, i * 0.07, 0.012, 0.072, 0.46)
        }
        play(ctx.toFloatArray())
    }

    fun playRoundEndSound() {
        val chordTones = listOf(261.63, 329.63, 392.00, 523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98)
        var idx = (0..4).random()
        val notesList = mutableListOf<Double>()
        repeat(6) {
            notesList.add(chordTones[idx])
            idx = (idx + listOf(-2, -1, 1, 2, 3).random()).coerceIn(0, chordTones.size - 1)
        }
        notesList.add(2093.0)
        val ctx = SynthContext(notesList.size * 0.085 + 0.75, sampleRate)
        for ((i, freq) in notesList.withIndex()) {
            ctx.addOsc(OscType.SINE, freq, i * 0.085, 0.01, 0.3, 0.7)
            ctx.addOsc(OscType.TRIANGLE, freq * 2, i * 0.085, 0.01, 0.088, 0.7)
            ctx.addOsc(OscType.SINE, freq * 0.5, i * 0.085, 0.01, 0.106, 0.7)
        }
        play(ctx.toFloatArray())
    }

    fun playTickSound(secondsLeft: Int) {
        val freq = 600.0 + (10 - secondsLeft) * 66.0
        val ctx = SynthContext(0.15, sampleRate)
        ctx.addOsc(OscType.SINE, freq, 0.0, 0.008, if (secondsLeft <= 3) 0.34 else 0.24, 0.12)
        if (secondsLeft <= 3) ctx.addOsc(OscType.SINE, freq * 2, 0.0, 0.001, 0.1, 0.1)
        play(ctx.toFloatArray())
    }

    fun startPowerUpChimes(duration: Double) {
        stopPowerUpChimes()
        powerUpJob = scope.launch(Dispatchers.IO) {
            val chimeIntervalMs = ((duration * 1000) / powerUpChimeGroups.size).toLong()
            var step = 0
            playPowerUpChime(step, 0.0)
            step++
            while (isActive && step < powerUpChimeGroups.size) {
                delay(chimeIntervalMs)
                playPowerUpChime(step, step.toDouble() / powerUpChimeGroups.size)
                step++
            }
        }
    }

    fun stopPowerUpChimes() {
        powerUpJob?.cancel()
        powerUpJob = null
    }

    fun release() {
        stopPowerUpChimes()
        for (track in activeTracks) {
            try { track.stop(); track.release() } catch (_: Exception) {}
        }
        activeTracks.clear()
    }

    private fun addSparkle(ctx: SynthContext, step: Int, masterGain: Double) {
        val sparkleNotes = listOf(659.25, 698.46, 783.99, 880.00, 987.77, 1046.50,
            1174.66, 1318.51, 1396.91, 1567.98)
        val sparkleCount = min(4, max(3, step - 1))
        val sparkleGain = min(0.07, 0.024 + (step - 3) * 0.007) * masterGain
        val rootIndex = min(step - 3, sparkleNotes.size - 4)
        val phrase = sparkleNotes.drop(rootIndex).take(sparkleCount).toMutableList()
        if (step >= 7 && phrase.size > 1) {
            val last = phrase.removeLast()
            phrase.shuffle()
            phrase.add(last)
        }
        for ((i, freq) in phrase.withIndex()) {
            ctx.addOsc(OscType.SINE, freq, 0.055 + i * 0.06, 0.018, sparkleGain, 0.36,
                filter = FilterSpec(FilterKind.LOWPASS, 2400.0, 0.7))
        }
    }

    private val powerUpChimeGroups = listOf(
        listOf(2093.00, 1975.53, 1760.00), listOf(1975.53, 1760.00, 1567.98),
        listOf(1760.00, 1567.98, 1396.91), listOf(1567.98, 1396.91, 1318.51),
        listOf(1396.91, 1318.51, 1174.66), listOf(1318.51, 1174.66, 1046.50),
        listOf(1174.66, 1046.50, 987.77),  listOf(1046.50, 987.77, 880.00),
        listOf(987.77, 880.00, 783.99),    listOf(880.00, 783.99, 698.46),
        listOf(783.99, 698.46, 659.25),    listOf(698.46, 659.25, 587.33),
        listOf(659.25, 587.33, 523.25)
    )

    private fun playPowerUpChime(step: Int, progress: Double) {
        val notes = powerUpChimeGroups[min(step, powerUpChimeGroups.size - 1)]
        val level = max(0.012, 0.032 * (1 - progress))
        val ctx = SynthContext(notes.size * 0.115 + 0.6, sampleRate)
        for ((i, freq) in notes.withIndex()) {
            val dly = i * 0.08 + Math.random() * 0.035
            ctx.addOsc(OscType.SINE, freq, dly, 0.02, level, 0.55,
                filter = FilterSpec(FilterKind.LOWPASS, 2600.0, 0.7))
        }
        play(ctx.toFloatArray())
    }

    private fun play(samples: FloatArray) {
        scope.launch(Dispatchers.IO) {
            try {
                val minBuf = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT)
                val bufBytes = max(samples.size * 4, minBuf)
                val track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufBytes)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                activeTracks.add(track)
                track.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
                track.play()
                delay((samples.size.toLong() * 1000L / sampleRate) + 80L)
                track.stop()
                track.release()
                activeTracks.remove(track)
            } catch (_: Exception) {}
        }
    }
}

private enum class OscType { SINE, TRIANGLE }
private enum class FilterKind { LOWPASS, HIGHPASS, BANDPASS }
private data class FilterSpec(val kind: FilterKind, val frequency: Double, val q: Double)

private class SynthContext(duration: Double, private val sampleRate: Int) {
    val samples = FloatArray(ceil(duration * sampleRate).toInt())

    fun addOsc(
        type: OscType, freq: Double, start: Double, attackTime: Double, peakAmp: Double,
        releaseTime: Double, settleRatio: Double = 1.0, settleTime: Double? = null,
        filter: FilterSpec? = null
    ) {
        val startSample = (start * sampleRate).toInt()
        val attackSamples = max(1, (attackTime * sampleRate).toInt())
        val releaseSamples = max(attackSamples + 1, (releaseTime * sampleRate).toInt())
        val settleSamples = settleTime?.let { max(attackSamples + 1, (it * sampleRate).toInt()) }
        val rendered = FloatArray(samples.size)

        for (i in startSample until min(samples.size, startSample + releaseSamples)) {
            val t = i.toDouble() / sampleRate
            val raw = when (type) {
                OscType.SINE -> sin(2.0 * PI * freq * t)
                OscType.TRIANGLE -> { val p = t * freq; 2.0 * abs(2.0 * (p - floor(p + 0.5))) - 1.0 }
            }
            val elapsed = i - startSample
            val gain = when {
                elapsed < attackSamples -> peakAmp * elapsed.toDouble() / attackSamples
                settleSamples != null && elapsed < settleSamples -> {
                    val prog = (elapsed - attackSamples).toDouble() / max(1, settleSamples - attackSamples)
                    expRamp(peakAmp, peakAmp * settleRatio, prog)
                }
                else -> {
                    val rs = settleSamples ?: attackSamples
                    val prog = (elapsed - rs).toDouble() / max(1, releaseSamples - rs)
                    expRamp(peakAmp * settleRatio, 0.001, prog)
                }
            }
            rendered[i] = (raw * gain).toFloat()
        }
        if (filter != null) applyBiquad(filter, rendered)
        for (i in rendered.indices) samples[i] += rendered[i]
    }

    fun addOscWithFreqSlide(freq: Double, endFreq: Double, start: Double, duration: Double, peakAmp: Double) {
        val startSample = (start * sampleRate).toInt()
        val endSample = min(samples.size, ((start + duration) * sampleRate).toInt())
        var phase = 0.0
        val durationSamples = max(1, endSample - startSample)
        for (i in startSample until endSample) {
            val progress = (i - startSample).toDouble() / durationSamples
            phase += 2.0 * PI * freq * (endFreq / freq).pow(progress) / sampleRate
            samples[i] += (sin(phase) * peakAmp * exp(-progress * 3.5)).toFloat()
        }
    }

    fun addOscWithVibrato(
        type: OscType, freq: Double, start: Double, attackTime: Double, peakAmp: Double,
        releaseTime: Double, vibratoRate: Double, vibratoDepthCents: Double, vibratoDelay: Double,
        settleRatio: Double = 1.0, settleTime: Double? = null
    ) {
        val startSample = (start * sampleRate).toInt()
        val attackSamples = max(1, (attackTime * sampleRate).toInt())
        val releaseSamples = max(attackSamples + 1, (releaseTime * sampleRate).toInt())
        val settleSamples = settleTime?.let { max(attackSamples + 1, (it * sampleRate).toInt()) }
        var phase = 0.0

        for (i in startSample until min(samples.size, startSample + releaseSamples)) {
            val elapsed = i - startSample
            val et = elapsed.toDouble() / sampleRate
            val vibProg = ((et - vibratoDelay) / 0.18).coerceIn(0.0, 1.0)
            val vibCents = sin(2.0 * PI * vibratoRate * et) * vibratoDepthCents * vibProg
            phase += 2.0 * PI * freq * 2.0.pow(vibCents / 1200.0) / sampleRate
            val raw = when (type) {
                OscType.SINE -> sin(phase)
                OscType.TRIANGLE -> { val p = phase / (2.0 * PI); 2.0 * abs(2.0 * (p - floor(p + 0.5))) - 1.0 }
            }
            val gain = when {
                elapsed < attackSamples -> peakAmp * elapsed.toDouble() / attackSamples
                settleSamples != null && elapsed < settleSamples -> {
                    val prog = (elapsed - attackSamples).toDouble() / max(1, settleSamples - attackSamples)
                    expRamp(peakAmp, peakAmp * settleRatio, prog)
                }
                else -> {
                    val rs = settleSamples ?: attackSamples
                    val prog = (elapsed - rs).toDouble() / max(1, releaseSamples - rs)
                    expRamp(peakAmp * settleRatio, 0.001, prog)
                }
            }
            samples[i] += (raw * gain).toFloat()
        }
    }

    fun addNoise(start: Double, duration: Double, amplitude: Double, highpass: Boolean, bandpass: Boolean = false) {
        val startSample = (start * sampleRate).toInt()
        val noiseSamples = (duration * sampleRate).toInt()
        val endSample = min(samples.size, startSample + noiseSamples)
        var filterState = 0.0
        val cutoff = if (highpass) 0.85 else if (bandpass) 0.6 else 0.15
        for (i in startSample until endSample) {
            val raw = Math.random() * 2.0 - 1.0
            filterState = filterState * (1 - cutoff) + raw * cutoff
            val filtered = if (highpass) raw - filterState else filterState
            val progress = (i - startSample).toDouble() / max(noiseSamples, 1)
            samples[i] += (filtered * amplitude * exp(-progress * 5.0)).toFloat()
        }
    }

    fun toFloatArray(): FloatArray {
        val result = samples.clone()
        applyOutputPolish(result)
        return result
    }

    private fun applyOutputPolish(rendered: FloatArray) {
        val fadeSamples = min(rendered.size, (sampleRate * 0.014).toInt())
        if (fadeSamples > 1) {
            val start = rendered.size - fadeSamples
            for (i in 0 until fadeSamples) {
                rendered[start + i] *= 1f - i.toFloat() / (fadeSamples - 1).toFloat()
            }
        }
        for (i in rendered.indices) rendered[i] = tanh(rendered[i].toDouble() * 0.82).toFloat()
    }

    private fun expRamp(start: Double, end: Double, progress: Double): Double {
        val s = max(start, 0.0001); val e = max(end, 0.0001)
        return s * (e / s).pow(progress.coerceIn(0.0, 1.0))
    }

    private fun applyBiquad(spec: FilterSpec, rendered: FloatArray) {
        val cutoff = spec.frequency.coerceIn(20.0, sampleRate * 0.45)
        val omega = 2.0 * PI * cutoff / sampleRate
        val alpha = sin(omega) / (2.0 * max(spec.q, 0.001))
        val cosOmega = cos(omega)
        val coeff = when (spec.kind) {
            FilterKind.LOWPASS -> doubleArrayOf((1-cosOmega)/2, 1-cosOmega, (1-cosOmega)/2, 1+alpha, -2*cosOmega, 1-alpha)
            FilterKind.HIGHPASS -> doubleArrayOf((1+cosOmega)/2, -(1+cosOmega), (1+cosOmega)/2, 1+alpha, -2*cosOmega, 1-alpha)
            FilterKind.BANDPASS -> doubleArrayOf(alpha, 0.0, -alpha, 1+alpha, -2*cosOmega, 1-alpha)
        }
        val b0=coeff[0]; val b1=coeff[1]; val b2=coeff[2]; val a0=coeff[3]; val a1=coeff[4]; val a2=coeff[5]
        var x1=0.0; var x2=0.0; var y1=0.0; var y2=0.0
        for (i in rendered.indices) {
            val x0 = rendered[i].toDouble()
            val y0 = (b0/a0)*x0 + (b1/a0)*x1 + (b2/a0)*x2 - (a1/a0)*y1 - (a2/a0)*y2
            rendered[i] = y0.toFloat(); x2=x1; x1=x0; y2=y1; y1=y0
        }
    }
}
