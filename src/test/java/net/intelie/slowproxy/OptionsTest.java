package net.intelie.slowproxy;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class OptionsTest {
    @Test
    public void test_56k_1234_blabla_5678() throws Exception {
        Options options = Options.parse("@56k", "1234", "blabla:5678");

        assertThat(options.speed().maxDownloadBytes()).isEqualTo(56 * 1024 / 8);
        assertThat(options.speed().maxUploadBytes()).isEqualTo(56 * 1024 / 8);

        assertThat(options.local().host()).isEqualTo(null);
        assertThat(options.local().port()).isEqualTo(1234);

        assertThat(options.remote().size()).isEqualTo(1);
        assertThat(options.remote().get(0).host()).isEqualTo("blabla");
        assertThat(options.remote().get(0).port()).isEqualTo(5678);
    }

    @Test
    public void testDefineLocalAddress() throws Exception {
        Options options = Options.parse("@56k", "127.0.0.1:1234", "blabla:5678");

        assertThat(options.speed().maxDownloadBytes()).isEqualTo(56 * 1024 / 8);
        assertThat(options.speed().maxUploadBytes()).isEqualTo(56 * 1024 / 8);

        assertThat(options.local().host()).isEqualTo("127.0.0.1");
        assertThat(options.local().port()).isEqualTo(1234);

        assertThat(options.remote().size()).isEqualTo(1);
        assertThat(options.remote().get(0).host()).isEqualTo("blabla");
        assertThat(options.remote().get(0).port()).isEqualTo(5678);
    }

    @Test
    public void testNoSpeed() throws Exception {
        Options options = Options.parse("1234", "blabla:5678");

        assertThat(options.speed().maxDownloadBytes()).isEqualTo(-1);
        assertThat(options.speed().maxUploadBytes()).isEqualTo(-1);

        assertThat(options.local().host()).isEqualTo(null);
        assertThat(options.local().port()).isEqualTo(1234);

        assertThat(options.remote().size()).isEqualTo(1);
        assertThat(options.remote().get(0).host()).isEqualTo("blabla");
        assertThat(options.remote().get(0).port()).isEqualTo(5678);
    }

    @Test
    public void testMultipleHosts() throws Exception {
        Options options = Options.parse("1234", "blabla:5678", "xxx:4567");

        assertThat(options.speed().maxDownloadBytes()).isEqualTo(-1);
        assertThat(options.speed().maxUploadBytes()).isEqualTo(-1);

        assertThat(options.local().host()).isEqualTo(null);
        assertThat(options.local().port()).isEqualTo(1234);

        assertThat(options.remote().size()).isEqualTo(2);
        assertThat(options.remote().get(0).host()).isEqualTo("blabla");
        assertThat(options.remote().get(0).port()).isEqualTo(5678);

        assertThat(options.remote().get(1).host()).isEqualTo("xxx");
        assertThat(options.remote().get(1).port()).isEqualTo(4567);
    }
}