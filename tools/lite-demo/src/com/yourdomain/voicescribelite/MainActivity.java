package com.yourdomain.voicescribelite;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * VoiceScribe Lite -- a minimal, standalone audio recorder/player.
 *
 * This is a deliberately reduced build: it uses only the plain Android
 * framework (MediaRecorder/MediaPlayer, classic Views), with none of the
 * AndroidX/Compose/Room/Koin/SQLCipher stack the full VoiceScribe scaffold
 * is designed around. It exists so there is something real and installable
 * on-device today, built entirely offline; it is not a replacement for the
 * full app, which needs Android Studio (or CI with real network access) to
 * build against Google Maven / Maven Central.
 *
 * No transcription, no database, no encryption here -- just record, list,
 * and play back audio files stored in this app's private external files
 * directory.
 */
public class MainActivity extends Activity {

    private static final int REQUEST_RECORD_AUDIO = 1;

    private TextView statusText;
    private Button recordButton;
    private ListView recordingsList;

    private MediaRecorder recorder;
    private MediaPlayer player;
    private boolean isRecording = false;
    private boolean hasRecordings = false;
    private String currentPlayingPath = null;

    private File recordingsDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = (TextView) findViewById(R.id.statusText);
        recordButton = (Button) findViewById(R.id.recordButton);
        recordingsList = (ListView) findViewById(R.id.recordingsList);

        File externalDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        recordingsDir = externalDir != null ? externalDir : getFilesDir();
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs();
        }

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecordButtonClicked();
            }
        });

        recordingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String label = (String) parent.getItemAtPosition(position);
                onRecordingClicked(label);
            }
        });

        refreshRecordingsList();
    }

    private void onRecordButtonClicked() {
        if (isRecording) {
            stopRecording();
        } else {
            if (ContextCompatCheckSelfPermission()) {
                startRecording();
            } else {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            }
        }
    }

    private boolean ContextCompatCheckSelfPermission() {
        return checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(this, R.string.permission_needed, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startRecording() {
        stopPlaybackIfAny();

        String fileName = "recording_" + DateFormat.format("yyyyMMdd_HHmmss", new Date()) + ".m4a";
        File outFile = new File(recordingsDir, fileName);

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioEncodingBitRate(96000);
        recorder.setAudioSamplingRate(44100);
        recorder.setOutputFile(outFile.getAbsolutePath());

        try {
            recorder.prepare();
            recorder.start();
            isRecording = true;
            recordButton.setText(R.string.stop);
            statusText.setText(R.string.status_recording);
        } catch (Exception e) {
            Toast.makeText(this, "Could not start recording: " + e.getMessage(), Toast.LENGTH_LONG).show();
            recorder.release();
            recorder = null;
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException ignored) {
                // stop() throws if called too soon after start(); the partial
                // file is discarded below by refreshRecordingsList() simply
                // not finding a usable file, which is fine for this demo.
            }
            recorder.release();
            recorder = null;
        }
        isRecording = false;
        recordButton.setText(R.string.record);
        statusText.setText(R.string.status_idle);
        refreshRecordingsList();
    }

    private void onRecordingClicked(String label) {
        if (!hasRecordings) {
            return;
        }
        File file = new File(recordingsDir, label);
        if (file.getAbsolutePath().equals(currentPlayingPath)) {
            stopPlaybackIfAny();
            return;
        }
        stopPlaybackIfAny();
        try {
            player = new MediaPlayer();
            player.setDataSource(file.getAbsolutePath());
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaybackIfAny();
                }
            });
            player.prepare();
            player.start();
            currentPlayingPath = file.getAbsolutePath();
            statusText.setText(getString(R.string.status_playing) + " " + label);
        } catch (Exception e) {
            Toast.makeText(this, "Could not play: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void stopPlaybackIfAny() {
        if (player != null) {
            try {
                player.stop();
            } catch (RuntimeException ignored) {
            }
            player.release();
            player = null;
        }
        currentPlayingPath = null;
        if (!isRecording) {
            statusText.setText(R.string.status_idle);
        }
    }

    private void refreshRecordingsList() {
        File[] files = recordingsDir.listFiles();
        List<String> names = new ArrayList<String>();
        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File a, File b) {
                    return Long.valueOf(b.lastModified()).compareTo(a.lastModified());
                }
            });
            for (File f : files) {
                if (f.isFile() && f.length() > 0) {
                    names.add(f.getName());
                }
            }
        }
        hasRecordings = !names.isEmpty();
        if (!hasRecordings) {
            names.add(getString(R.string.no_recordings));
            recordingsList.setEnabled(false);
        } else {
            recordingsList.setEnabled(true);
        }
        recordingsList.setAdapter(new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, names));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recorder != null) {
            try {
                recorder.release();
            } catch (RuntimeException ignored) {
            }
        }
        stopPlaybackIfAny();
    }
}
