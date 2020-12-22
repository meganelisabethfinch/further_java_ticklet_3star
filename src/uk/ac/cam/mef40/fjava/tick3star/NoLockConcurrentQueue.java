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

import java.util.concurrent.atomic.AtomicReference;

public class NoLockConcurrentQueue<T> implements ConcurrentQueue<T> {
  private static class Link<L> {
    final L val;
    AtomicReference<Link<L>> next;

    Link(L val, AtomicReference<Link<L>> next) {
      this.val = val;
      this.next = next == null ? new AtomicReference<>(null) : next;
    }
  }

  private Link<T> dummyNode = new Link<>(null, null);
  private AtomicReference<Link<T>> Head;
  private AtomicReference<Link<T>> Tail;

  public NoLockConcurrentQueue() {
    Head = new AtomicReference<>(dummyNode);
    Tail = new AtomicReference<>(dummyNode);
  }

  public void offer(T message) {
    Link<T> newNode = new Link<>(message, null);
    Link<T> tail;
    while (true) {
      tail = Tail.get();
      Link<T> next = tail.next.get();
      if (tail == Tail.get()) {
        if (next == null) {
          if (tail.next.compareAndSet(next, newNode)) {
            break;
          }
        } else {
          Tail.compareAndSet(tail, next);
        }
      }
    }
    Tail.compareAndSet(tail, newNode);
  }

  public T poll() {
    T val;

    while (true) {
      Link<T> head = Head.get();
      Link<T> tail = Tail.get();
      Link<T> next = head.next.get();
      if (head == Head.get()) {
        if (head == tail) {
          if (next == null) {
            return null;
          }
          Tail.compareAndSet(tail, next);
        } else {
          val = next.val;
          if (Head.compareAndSet(head, next)) {
            break;
          }
        }
      }
    }
    return val;
  }
}
