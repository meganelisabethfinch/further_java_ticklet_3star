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

import javax.swing.*;
import java.util.concurrent.atomic.AtomicReference;

public class NoLockConcurrentQueue<T> implements ConcurrentQueue<T> {
  private static class Link<L> {
    L val;
    Link<L> next;

    Link(L val) {
      this.val = val;
      this.next = null;
    }
  }

  private Link<T> Head;
  private Link<T> Tail;

  public NoLockConcurrentQueue() {
    Link<T> node = new Link<>(null);
    Head = null;
    Tail = null;
  }

  public void offer(T message) {
    Link<T> node = new Link<>(message); // Allocate new node with value
    Link<T> tail;
    while (true) { // Keep trying until enqueue done
      tail = Tail; // read Tail
      var next = tail.next;
      if (tail == Tail) {
        if (next.next == null) {
          AtomicReference<Link<T>> ar = new AtomicReference<>(tail.next);
          if (ar.compareAndSet(next, node)) {
            break;
          }
        } else {
          AtomicReference<Link<T>> ar = new AtomicReference<>(Tail);
          ar.compareAndSet(tail, next.next);
        }
      }
    }
    AtomicReference<Link<T>> ar = new AtomicReference<>(Tail);
    ar.compareAndSet(tail, node); // Enqueue done. Try to swing tail to inserted node.
    System.out.println("Offered " + message);
  }

  public T poll() {
    T val;
    while (true) {
      var head = Head;
      var tail = Tail;
      var next = head.next;
      if (head == Head) {
        if (head.next == tail.next) {
          if (next.next == null) {
            System.out.println("Polled null");
            return null; // Queue empty. Couldn't dequeue.
          }
          new AtomicReference<>(Tail).compareAndSet(tail, next.next);
        } else {
          val = next.next.val;
          if (new AtomicReference<>(Head).compareAndSet(head, next.next)) {
            break;
          }
        }
      }
    }
    System.out.println("Polled " + val);
    return val;
  }
}
