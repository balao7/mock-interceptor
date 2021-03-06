package com.example.chriskoeberle.githubexample.home.endpoint;


import com.bottlerocketstudios.groundcontrol.convenience.GroundControl;
import com.example.chriskoeberle.githubexample.home.construction.ServiceInjector;
import com.example.chriskoeberle.githubexample.home.construction.ServiceLocator;
import com.example.chriskoeberle.githubexample.home.model.User;

import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import okhttp3.OkHttpClient;

import static org.junit.Assert.assertEquals;

public class UserTest extends BaseApiTest {

    @Test
    public void testUser() {
        ServiceLocator.put(OkHttpClient.class, OkHttpClientUtil.getOkHttpClient(null, MockBehavior.MOCK));
        Flowable<User> flowable = ServiceInjector.resolve(RxEndpoints.class).getUser("bottlerocketapps");
        TestSubscriber<User> testSubscriber = new TestSubscriber<>();
        flowable.subscribe(testSubscriber);
        testSubscriber.assertComplete();
        List<User> userList = testSubscriber.values();
        assertEquals(userList.size(), 1);
        assertEquals(userList.get(0).getName(), "Bottle Rocket");

    }

    @Test
    public void testUserWithGroundControl() throws InterruptedException {
        ServiceLocator.put(OkHttpClient.class, OkHttpClientUtil.getOkHttpClient(null, MockBehavior.MOCK));
        CountDownLatch latch = new CountDownLatch(1);
        TestListener<User> listener = new TestListener<User>() { };
        GroundControl.agent(new UserAgent("bottlerocketstudios"))
                .bgParallelCallback(listener.withLatch(latch))
                .execute();
        latch.await(5, TimeUnit.SECONDS);
        ResultWrapper<User> wrapper = listener.getResultWrapper();
        assertEquals(HttpURLConnection.HTTP_OK, wrapper.getErrorCode());
        assertEquals("OK", wrapper.getErrorMessage());
        assertEquals("Bottle Rocket Studios", wrapper.getResult().getName());
    }
}
