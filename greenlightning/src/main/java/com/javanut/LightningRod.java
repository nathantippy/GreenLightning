package com.javanut;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javanut.gl.api.ArgumentParser;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.test.ParallelClientLoadTester;
import com.javanut.gl.test.ParallelClientLoadTesterConfig;
import com.javanut.gl.test.ParallelClientLoadTesterPayload;

public class LightningRod {

    static final Logger logger = LoggerFactory.getLogger(GreenLightning.class);

    public static void main(String[] args) {
        ArgumentParser parser = new ArgumentParser(args);
        ParallelClientLoadTesterConfig config = new ParallelClientLoadTesterConfig(parser);
        ParallelClientLoadTesterPayload payload = new ParallelClientLoadTesterPayload(parser);

        GreenRuntime.run(new ParallelClientLoadTester(config, payload), args);
    }
}
