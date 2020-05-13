package me.yukino.fireprocess.controller;

import me.yukino.fireprocess.model.CellPredictModelProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Hoshiiro Yukino
 */

@Component
public class ModelPredictMessageQueue {

    private AtomicInteger count;

    public ModelPredictMessageQueue(){
        count = new AtomicInteger();
    }

    public int requirePredict() {
        return count.addAndGet(1);
    }

    public AtomicInteger getCount() {
        return count;
    }

    @Component
    class ModelPredictConsumer {

        @Scheduled(cron = "0/5 * * * * ?")
        public void consumePredict() {
            if (getCount().get() > 0) {
                getCount().set(0);
                CellPredictModelProvider.getCellPredictModel().predict(10 * 60 * 1000);
            }
        }

    }

}
