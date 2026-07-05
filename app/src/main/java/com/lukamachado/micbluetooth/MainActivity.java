package com.lukamachado.micbluetooth;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.Equalizer;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final String PREFS_NAME = "MicBluetoothPrefs";
    
    private int getBufferSize() {
        int minSize = Math.max(
                AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN, AUDIO_FORMAT),
                AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT, AUDIO_FORMAT)
        );
        return isStabilityMode ? minSize * 3 : minSize;
    }

    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private Equalizer equalizer;
    private AcousticEchoCanceler echoCanceler;
    private NoiseSuppressor noiseSuppressor;
    private AutomaticGainControl agc;
    private Thread audioThread;
    
    private volatile boolean isRecording = false; 
    private volatile float echoIntensity = 0.0f;
    private volatile int sensitivityThreshold = 0;
    private volatile float voiceBoost = 1.0f;
    private volatile boolean isStabilityMode = false;

    private LinearLayout transmissionSection;
    private LinearLayout equalizerLayout;
    private TextView echoValueText;
    private TextView sensitivityValueText;
    private TextView deviceStatusText;
    private ProgressBar vuMeter;
    private AudioManager audioManager;

    // NOVO: Ouvinte de mudança de dispositivo em tempo real
    private AudioDeviceCallback deviceCallback;

    private final BroadcastReceiver noisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                stopAudio();
                updateDeviceStatus();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        loadSettings();

        transmissionSection = findViewById(R.id.transmission_section);
        equalizerLayout = findViewById(R.id.equalizer_layout);
        echoValueText = findViewById(R.id.echo_value_text);
        deviceStatusText = findViewById(R.id.device_status_text);
        vuMeter = findViewById(R.id.vu_meter);
        SeekBar echoSeekBar = findViewById(R.id.echo_seekbar);
        ImageButton btnClose = findViewById(R.id.btn_close);
        
        setupTransmissionControls();
        setupDeviceDetection(); // Configura o monitoramento constante
        
        echoSeekBar.setProgress((int) (echoIntensity * 100));
        echoValueText.setText(getString(R.string.intensity_label, (int) (echoIntensity * 100)));
        echoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                echoIntensity = progress / 100f;
                echoValueText.setText(getString(R.string.intensity_label, progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnClose.setOnClickListener(v -> finish());
        
        registerReceiver(noisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
    }

    private void setupDeviceDetection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            deviceCallback = new AudioDeviceCallback() {
                @Override
                public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
                    runOnUiThread(() -> updateDeviceStatus());
                }

                @Override
                public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
                    runOnUiThread(() -> updateDeviceStatus());
                }
            };
            audioManager.registerAudioDeviceCallback(deviceCallback, new Handler(Looper.getMainLooper()));
        }
    }

    private void updateDeviceStatus() {
        String deviceName = getString(R.string.internal_speaker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioDeviceInfo[] outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
            for (AudioDeviceInfo device : outputs) {
                int type = device.getType();
                if (type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || 
                    type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    type == AudioDeviceInfo.TYPE_USB_HEADSET) {
                    
                    String name = device.getProductName().toString();
                    if (name != null && !name.isEmpty() && !name.equals("null")) {
                        deviceName = name;
                    } else {
                        deviceName = "Dispositivo Externo";
                    }
                    break;
                }
            }
        }
        deviceStatusText.setText(deviceName);
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        echoIntensity = prefs.getFloat("echoIntensity", 0.0f);
        sensitivityThreshold = prefs.getInt("sensitivityThreshold", 0);
        voiceBoost = prefs.getFloat("voiceBoost", 1.0f);
        isStabilityMode = prefs.getBoolean("isStabilityMode", false);
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putFloat("echoIntensity", echoIntensity);
        editor.putInt("sensitivityThreshold", sensitivityThreshold);
        editor.putFloat("voiceBoost", voiceBoost);
        editor.putBoolean("isStabilityMode", isStabilityMode);
        
        if (equalizer != null) {
            editor.putInt("tone_bass", getToneProgress(0, 300000));
            editor.putInt("tone_mid", getToneProgress(300001, 3000000));
            editor.putInt("tone_treble", getToneProgress(3000001, 20000000));
        }
        editor.apply();
    }

    private int getToneProgress(int minFreq, int maxFreq) {
        if (equalizer == null) return 0;
        for (short b = 0; b < equalizer.getNumberOfBands(); b++) {
            int centerFreq = equalizer.getCenterFreq(b);
            if (centerFreq >= minFreq && centerFreq <= maxFreq) {
                return equalizer.getBandLevel(b) - equalizer.getBandLevelRange()[0];
            }
        }
        return 0;
    }

    private void setupTransmissionControls() {
        transmissionSection.removeAllViews();
        
        TextView boostTitle = new TextView(this);
        boostTitle.setText(R.string.mic_volume_label);
        boostTitle.setTextColor(Color.parseColor("#6200EE"));
        boostTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        transmissionSection.addView(boostTitle);

        SeekBar boostSeekBar = new SeekBar(this);
        boostSeekBar.setMax(400); 
        boostSeekBar.setProgress((int)((voiceBoost - 1.0f) * 100));
        boostSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                voiceBoost = 1.0f + (progress / 100f);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        transmissionSection.addView(boostSeekBar);

        android.view.View sep1 = new android.view.View(this);
        sep1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 30));
        transmissionSection.addView(sep1);

        TextView filterTitle = new TextView(this);
        filterTitle.setText(R.string.external_noise_block);
        filterTitle.setTextColor(Color.parseColor("#6200EE"));
        filterTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        transmissionSection.addView(filterTitle);

        sensitivityValueText = new TextView(this);
        updateSensitivityText(sensitivityThreshold);
        sensitivityValueText.setTextSize(12);
        transmissionSection.addView(sensitivityValueText);

        SeekBar sensSeekBar = new SeekBar(this);
        sensSeekBar.setMax(3000); 
        sensSeekBar.setProgress(sensitivityThreshold);
        sensSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sensitivityThreshold = progress;
                updateSensitivityText(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        transmissionSection.addView(sensSeekBar);

        android.view.View sep2 = new android.view.View(this);
        sep2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 30));
        transmissionSection.addView(sep2);

        CheckBox stabilityCheck = new CheckBox(this);
        stabilityCheck.setText(R.string.stability_mode_label);
        stabilityCheck.setChecked(isStabilityMode);
        stabilityCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isStabilityMode = isChecked;
            restartAudio();
        });
        transmissionSection.addView(stabilityCheck);

        TextView stabilityHint = new TextView(this);
        stabilityHint.setText(R.string.stability_mode_hint);
        stabilityHint.setTextSize(11);
        stabilityHint.setAlpha(0.6f);
        transmissionSection.addView(stabilityHint);
    }

    private void restartAudio() {
        stopAudio();
        startAudio();
    }

    private void updateSensitivityText(int progress) {
        if (progress == 0) sensitivityValueText.setText(R.string.mode_always_on);
        else if (progress > 2000) sensitivityValueText.setText(R.string.mode_speak_close);
        else sensitivityValueText.setText(R.string.mode_balanced);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDeviceStatus();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startAudio();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSettings();
        stopAudio();
    }

    private void startAudio() {
        if (isRecording) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        int bufferSize = getBufferSize();
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE, CHANNEL_IN, AUDIO_FORMAT, bufferSize);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AUDIO_FORMAT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(CHANNEL_OUT)
                            .build())
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setPerformanceMode(isStabilityMode ? AudioTrack.PERFORMANCE_MODE_NONE : AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                    .build();
        } else {
            audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE, CHANNEL_OUT, AUDIO_FORMAT, bufferSize, AudioTrack.MODE_STREAM);
        }

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED || audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            return;
        }

        int sessionId = audioRecord.getAudioSessionId();
        if (AutomaticGainControl.isAvailable()) {
            agc = AutomaticGainControl.create(sessionId);
            if (agc != null) agc.setEnabled(true);
        }
        if (AcousticEchoCanceler.isAvailable()) {
            echoCanceler = AcousticEchoCanceler.create(sessionId);
            if (echoCanceler != null) echoCanceler.setEnabled(true);
        }
        if (NoiseSuppressor.isAvailable()) {
            noiseSuppressor = NoiseSuppressor.create(sessionId);
            if (noiseSuppressor != null) noiseSuppressor.setEnabled(true);
        }

        equalizer = new Equalizer(0, audioTrack.getAudioSessionId());
        equalizer.setEnabled(true);

        setupSimplifiedUI();
        audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

        isRecording = true;
        audioThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int readSize = bufferSize / 2;
                short[] buffer = new short[readSize];
                int delaySamples = (int) (SAMPLE_RATE * 0.15); 
                short[] delayBuffer = new short[delaySamples];
                int delayIndex = 0;

                float currentGain = 0.0f;
                int holdCounter = 0;
                final int HOLD_SAMPLES = (int) (SAMPLE_RATE * 0.4); 

                audioRecord.startRecording();
                audioTrack.play();
                
                while (isRecording) {
                    int read = audioRecord.read(buffer, 0, buffer.length);
                    if (read > 0) {
                        double sum = 0;
                        for (int i = 0; i < read; i++) sum += buffer[i] * buffer[i];
                        double rms = Math.sqrt(sum / read);

                        final int level = (int) Math.min(100, (rms / 3276.7) * 100);
                        runOnUiThread(() -> vuMeter.setProgress(level));

                        float targetGain;
                        if (rms > sensitivityThreshold) {
                            targetGain = voiceBoost; 
                            holdCounter = HOLD_SAMPLES; 
                        } else {
                            if (holdCounter > 0) {
                                holdCounter -= read;
                                targetGain = voiceBoost;
                            } else {
                                targetGain = 0.0f; 
                            }
                        }

                        for (int i = 0; i < read; i++) {
                            currentGain += (targetGain - currentGain) * 0.15f; 
                            int sample = (int) (buffer[i] * currentGain);
                            
                            int mixed = sample;
                            if (echoIntensity > 0.01f) {
                                short echo = delayBuffer[delayIndex];
                                mixed += (int)(echo * echoIntensity);
                                delayBuffer[delayIndex] = (short) (sample / Math.max(1, voiceBoost));
                                delayIndex = (delayIndex + 1) % delaySamples;
                            }
                            
                            if (mixed > 32767) mixed = 32767;
                            else if (mixed < -32768) mixed = -32768;
                            
                            buffer[i] = (short) mixed;
                        }
                        audioTrack.write(buffer, 0, read);
                    }
                }
            }
        });
        audioThread.start();
    }

    private void stopAudio() {
        isRecording = false;
        if (audioManager != null) audioManager.abandonAudioFocus(null);
        if (audioThread != null) {
            try { audioThread.join(); } catch (InterruptedException e) {}
            audioThread = null;
        }
        if (agc != null) { agc.release(); agc = null; }
        if (echoCanceler != null) { echoCanceler.release(); echoCanceler = null; }
        if (noiseSuppressor != null) { noiseSuppressor.release(); noiseSuppressor = null; }
        if (audioRecord != null) { audioRecord.release(); audioRecord = null; }
        if (audioTrack != null) { audioTrack.release(); audioTrack = null; }
        if (equalizer != null) { equalizer.release(); equalizer = null; }
        runOnUiThread(() -> {
            if (vuMeter != null) vuMeter.setProgress(0);
        });
    }

    private void setupSimplifiedUI() {
        equalizerLayout.removeAllViews();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        addToneControl(getString(R.string.tone_bass), 0, 300000, prefs.getInt("tone_bass", -1));
        addToneControl(getString(R.string.tone_mid), 300001, 3000000, prefs.getInt("tone_mid", -1));
        addToneControl(getString(R.string.tone_treble), 3000001, 20000000, prefs.getInt("tone_treble", -1));
    }

    private void addToneControl(String label, final int minFreq, final int maxFreq, int savedProgress) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setPadding(0, 10, 0, 10);
        tv.setTextSize(14);
        equalizerLayout.addView(tv);

        SeekBar bar = new SeekBar(this);
        final short minLevel = equalizer.getBandLevelRange()[0];
        final short maxLevel = equalizer.getBandLevelRange()[1];
        bar.setMax(maxLevel - minLevel);
        
        int defaultProgress = (maxLevel - minLevel) / 2;
        int currentProgress = (savedProgress == -1) ? defaultProgress : savedProgress;
        bar.setProgress(currentProgress);

        short initialLevel = (short) (currentProgress + minLevel);
        applyToneLevel(minFreq, maxFreq, initialLevel);

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                short level = (short) (progress + minLevel);
                applyToneLevel(minFreq, maxFreq, level);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        equalizerLayout.addView(bar);
    }

    private void applyToneLevel(int minFreq, int maxFreq, short level) {
        if (equalizer == null) return;
        for (short b = 0; b < equalizer.getNumberOfBands(); b++) {
            int centerFreq = equalizer.getCenterFreq(b);
            if (centerFreq >= minFreq && centerFreq <= maxFreq) {
                equalizer.setBandLevel(b, level);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceCallback != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.unregisterAudioDeviceCallback(deviceCallback);
        }
        try { unregisterReceiver(noisyReceiver); } catch (Exception e) {}
        saveSettings();
        stopAudio();
    }
}
