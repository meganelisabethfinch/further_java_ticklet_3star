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

public class OneLockConcurrentQueue<T> implements ConcurrentQueue<T> {
  private static class Link<L> {
    L val;
    Link<L> next;

    Link(L val) {
      this.val = val;
      this.next = null;
    }
  }

  private Link<T> first = null;
  private Link<T> last = null;

  public synchronized void offer(T message) {
    Link<T> link = new Link<>(message);
    if (first == null) {
      // if queue empty
      first = link;
      last = link;
    } else {
      last.next = link;
      last = link;
    }
  }

  public synchronized T poll() {
    if (first != null) {
      T val = first.val;
      first = first.next;
      return val;
    }
    return null;
  }
}
