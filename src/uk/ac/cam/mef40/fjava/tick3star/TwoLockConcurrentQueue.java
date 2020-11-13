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

public class TwoLockConcurrentQueue<T> implements ConcurrentQueue<T> {
  private static class Link<L> {
    L val;
    Link<L> next;

    Link(L val) {
      this.val = val;
      this.next = null;
    }
  }

  private static class Lock {
    boolean isLocked;

    Lock() {
      this.isLocked = false;
    }

    synchronized void lock() throws InterruptedException {
      while (isLocked) {
        this.wait();
      }
      isLocked = true;
    }

    synchronized void unlock() {
      isLocked = false;
      this.notify();
    }
  }

  private Link<T> head;
  private Link<T> tail;
  private Lock headLock;
  private Lock tailLock;

  public TwoLockConcurrentQueue() {
    Link<T> node = new Link<>(null);
    head = node;
    tail = node;
    headLock = new Lock();
    tailLock = new Lock();
  }

  public void offer(T message)  {
    try {
      Link<T> node = new Link<>(message);
      tailLock.lock();
      tail.next = node;
      tail = node;
      tailLock.unlock();
    } catch (InterruptedException ignored) {

    }
  }

  public T poll() {
    try {
      headLock.lock();
      var newHead = head.next; // Read next pointer

      if (newHead == null) {
        headLock.unlock();
        return null; // Queue was empty
      }

      T val = newHead.val; // Queue not empty. Read value before release.
      head = newHead; // Swing head to next node
      headLock.unlock();
      return val; // Queue not empty. Poll successful.
    } catch (InterruptedException ignored) {

    }
    return null;
  }
}
