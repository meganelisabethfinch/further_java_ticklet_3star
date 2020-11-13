/*
 * Copyright 2020 Andrew Rice <acr31@cam.ac.uk>, Alastair Beresford <arb33@cam.ac.uk>, M.E. Finch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.cam.mef40.fjava.tick3star;

import java.util.Arrays;

public class ConcurrentQueueTest {

  private ConcurrentQueue<String> q;

  private class Producer extends Thread {
    private int sent = 0;

    public void run() {
      for (int i = 0; i < 50000; ++i) {
        q.offer("" + i);
        sent++;
      }
    }

    public int numberProduced() {
      return sent;
    }
  }

  private class Consumer extends Thread {
    private int recv = 0;

    public void run() {
      String r;
      while ((r = q.poll()) == null || !r.equals("EOF")) {
        if (r != null) {
          recv++;
        } else {
          Thread.yield();
        }
      }
      q.offer("EOF");
    }

    public int numberConsumed() {
      return recv;
    }
  }

  private Consumer[] consumers;
  private Producer[] producers;

  ConcurrentQueueTest(ConcurrentQueue<String> q, int c, int p) {
    this.q = q;
    consumers = new Consumer[c];
    for (int i = 0; i < c; ++i) {
      consumers[i] = new Consumer();
    }
    producers = new Producer[p];
    for (int i = 0; i < p; ++i) {
      producers[i] = new Producer();
    }
  }

  public boolean run() {

    for (Consumer c : consumers) {
      c.start();
    }
    for (Producer p : producers) {
      p.start();
    }
    for (Producer p : producers) {
      try {
        p.join();
      } catch (InterruptedException e) {
      }
    }
    q.offer("EOF");
    for (Consumer c : consumers) {
      try {
        c.join(10000);
      } catch (InterruptedException e) {
        // IGNORED exception
      }
    }
    int recv = Arrays.stream(consumers).mapToInt(Consumer::numberConsumed).sum();
    int sent = Arrays.stream(producers).mapToInt(Producer::numberProduced).sum();

    System.out.printf(
        "%s (%d,%d):\t%s%n",
        q.getClass().getSimpleName(),
        consumers.length,
        producers.length,
        recv == sent ? "PASS" : "FAIL");
    return recv == sent;
  }

  public static void main(String[] args) {
    for (int i = 0; i < 10; ++i) {
      if (!new ConcurrentQueueTest(new OneLockConcurrentQueue<String>(), 5, 5).run()) return;
      if (!new ConcurrentQueueTest(new TwoLockConcurrentQueue<String>(), 5, 5).run()) return;
      if (!new ConcurrentQueueTest(new NoLockConcurrentQueue<String>(), 5, 5).run()) return;
    }
  }
}
