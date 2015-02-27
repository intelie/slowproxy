package net.intelie.slowproxy;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class SpeedDefinitionTest {
    @Test
    public void test56kbps() throws Exception {
        SpeedDefinition def = SpeedDefinition.parse("56k");

        assertThat(def.maxDownloadBytes()).isEqualTo(56 * 1024 / 8);
        assertThat(def.maxUploadBytes()).isEqualTo(56 * 1024 / 8);
        assertThat(def.uploadDelay()).isEqualTo(0);
        assertThat(def.downloadDelay()).isEqualTo(0);
        assertThat(def.splitUpDown()).isFalse();
    }

    @Test
    public void test56kbps200ms() throws Exception {
        SpeedDefinition def = SpeedDefinition.parse("56k:200ms");

        assertThat(def.maxDownloadBytes()).isEqualTo(56 * 1024 / 8);
        assertThat(def.maxUploadBytes()).isEqualTo(56 * 1024 / 8);
        assertThat(def.uploadDelay()).isEqualTo(200);
        assertThat(def.downloadDelay()).isEqualTo(200);
        assertThat(def.splitUpDown()).isFalse();
    }

    @Test
    public void test56M() throws Exception {
        SpeedDefinition def = SpeedDefinition.parse("56Mbps");

        assertThat(def.maxDownloadBytes()).isEqualTo(56 * 1024 * 1024 / 8);
        assertThat(def.maxUploadBytes()).isEqualTo(56 * 1024 * 1024 / 8);
        assertThat(def.uploadDelay()).isEqualTo(0);
        assertThat(def.downloadDelay()).isEqualTo(0);
        assertThat(def.splitUpDown()).isFalse();
    }

    @Test
    public void test56M2seconds() throws Exception {
        SpeedDefinition def = SpeedDefinition.parse("56Mbps:2s");

        assertThat(def.maxDownloadBytes()).isEqualTo(56 * 1024 * 1024 / 8);
        assertThat(def.maxUploadBytes()).isEqualTo(56 * 1024 * 1024 / 8);
        assertThat(def.uploadDelay()).isEqualTo(2000);
        assertThat(def.downloadDelay()).isEqualTo(2000);
        assertThat(def.splitUpDown()).isFalse();
    }

    @Test
    public void test56kbpsDown2mbpsUp() throws Exception {
        SpeedDefinition def = SpeedDefinition.parse("56kbps/2mbps");

        assertThat(def.maxDownloadBytes()).isEqualTo(56 * 1024 / 8);
        assertThat(def.maxUploadBytes()).isEqualTo(2 * 1024 * 1024 / 8);
        assertThat(def.uploadDelay()).isEqualTo(0);
        assertThat(def.downloadDelay()).isEqualTo(0);
        assertThat(def.splitUpDown()).isTrue();
    }

    @Test
    public void test56kbpsDown2mbpsUpWithDelay() throws Exception {
        SpeedDefinition def = SpeedDefinition.parse("56kbps:2s/2mbps:1ms");

        assertThat(def.maxDownloadBytes()).isEqualTo(56 * 1024 / 8);
        assertThat(def.maxUploadBytes()).isEqualTo(2 * 1024 * 1024 / 8);
        assertThat(def.uploadDelay()).isEqualTo(1);
        assertThat(def.downloadDelay()).isEqualTo(2000);
        assertThat(def.splitUpDown()).isTrue();
    }

    @Test
    public void testBufferSize() throws Exception {
        SpeedDefinition def = SpeedDefinition.parse("56kbps:1500ms/2mbps:1ms");

        assertThat(def.downloadBufferSize()).isEqualTo(112 * 1024 / 8);
        assertThat(def.uploadBufferSize()).isEqualTo(2 * 1024 * 1024 / 8);
    }

    @Test
    public void testBufferSizeFull() throws Exception {
        SpeedDefinition def = SpeedDefinition.parse("56kbps:2000ms/2mbps:1000ms");

        assertThat(def.downloadBufferSize()).isEqualTo(112 * 1024 / 8);
        assertThat(def.uploadBufferSize()).isEqualTo(2 * 1024 * 1024 / 8);
    }
}