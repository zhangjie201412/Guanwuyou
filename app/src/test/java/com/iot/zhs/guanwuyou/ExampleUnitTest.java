package com.iot.zhs.guanwuyou;

import android.util.Log;

import org.junit.Test;
import org.litepal.util.LogUtil;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);

        for(int i=0;i<100;i++) {
            int number = new Random().nextInt(4);//[0-3]的随机数
            number = number + 1;
            System.out.println("number=" + number);
        }
    }
}