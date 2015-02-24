package net.intelie.slowproxy;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class SpeedDefinitionTest {
    @Test
    public void test56kbps() throws Exception {
        SpeedDefinition def = SpeedDefinition.parse("@56k");

        assertThat(def.maxDownloadBytes()).isEqualTo(56 * 1024 / 8);
        assertThat(def.maxUploadBytes()).isEqualTo(56 * 1024 / 8);
    }

    @Test
    public void test56M() throws Exception {
        SpeedDefinition def = SpeedDefinition.parse("@56Mbps");

        assertThat(def.maxDownloadBytes()).isEqualTo(56 * 1024 * 1024 / 8);
        assertThat(def.maxUploadBytes()).isEqualTo(56 * 1024 * 1024 / 8);
    }

    @Test
    public void test56kbpsDown2mbpsUp() throws Exception {
        SpeedDefinition def = SpeedDefinition.parse("@56kbps/2mbps");

        assertThat(def.maxDownloadBytes()).isEqualTo(56 * 1024 / 8);
        assertThat(def.maxUploadBytes()).isEqualTo(2 * 1024 * 1024 / 8);
    }
}