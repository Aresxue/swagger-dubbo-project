package com.come2future.boot.swagger.dubbo.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author: Ares
 * @time: 2021-10-25 18:59:00
 * @description: collection util
 * @version: JDK 1.8
 */
public class CollectionUtil {

  public static <T> boolean isEmpty(Collection<T> collection) {
    return (collection == null || collection.isEmpty());
  }

  public static <T> boolean isNotEmpty(Collection<T> collection) {
    return !isEmpty(collection);
  }

  public static <E> HashSet<E> newHashSet(int expectedSize) {
    return new HashSet<E>(MapUtil.capacity(expectedSize));
  }

  public static <E> HashSet<E> newHashSet() {
    return newHashSet(16);
  }

  public static <T> boolean containsInstance(Collection<T> collection, Object element) {
    if (collection != null) {
      for (Object candidate : collection) {
        if (candidate == element) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @author: Ares
   * @description: compare two collection
   * @time: 2021/12/18 16:46
   * @params: [source, target] request
   * @return: boolean response
   */
  public static <T> boolean equals(Collection<T> source, Collection<T> target) {
    return equals(source, target, Collection::hashCode);
  }

  /**
   * @author: Ares
   * @description: compare two collection use way of compute hashCode
   * @time: 2021/12/18 16:46
   * @params: [source, target, callback] request
   * @return: boolean response
   */
  public static <T> boolean equals(Collection<T> source, Collection<T> target,
      HashCallback<T> callback) {
    if (isEmpty(source) && isNotEmpty(target)) {
      return false;
    }
    if (isNotEmpty(source) && isEmpty(target)) {
      return false;
    }
    if (null == source && null == target) {
      return true;
    }
    if (source.size() != target.size()) {
      return false;
    }

    return callback.computeHashCode(source) == callback.computeHashCode(target);
  }

  public static <T> boolean containsAny(Collection<T> source, Collection<T> candidates) {
    return findFirstMatch(source, candidates) != null;
  }

  public static <T> T random(List<T> list) {
    if (CollectionUtil.isEmpty(list)) {
      return null;
    }
    int index = ThreadLocalRandom.current().nextInt(list.size());
    return list.get(index);
  }


  public static <SOURCE, E> E findFirstMatch(Collection<SOURCE> source, Collection<E> candidates) {
    if (isEmpty(source) || isEmpty(candidates)) {
      return null;
    }
    for (Object candidate : candidates) {
      if (source.contains(candidate)) {
        return (E) candidate;
      }
    }
    return null;
  }


  public static <T> T findValueOfType(Collection<T> collection, Class<T> type) {
    if (isEmpty(collection)) {
      return null;
    }
    T value = null;
    for (Object element : collection) {
      if (type == null || type.isInstance(element)) {
        if (value != null) {
          // More than one value found... no clear single value.
          return null;
        }
        value = (T) element;
      }
    }
    return value;
  }


  public static <T> boolean hasUniqueObject(Collection<T> collection) {
    if (isEmpty(collection)) {
      return false;
    }
    boolean hasCandidate = false;
    Object candidate = null;
    for (Object elem : collection) {
      if (!hasCandidate) {
        hasCandidate = true;
        candidate = elem;
      } else if (candidate != elem) {
        return false;
      }
    }
    return true;
  }

  public static <T> Class<?> findCommonElementType(Collection<T> collection) {
    if (isEmpty(collection)) {
      return null;
    }
    Class<?> candidate = null;
    for (Object val : collection) {
      if (val != null) {
        if (candidate == null) {
          candidate = val.getClass();
        } else if (candidate != val.getClass()) {
          return null;
        }
      }
    }
    return candidate;
  }


  public static <T> T firstElement(Set<T> set) {
    if (isEmpty(set)) {
      return null;
    }
    if (set instanceof SortedSet) {
      return ((SortedSet<T>) set).first();
    }

    Iterator<T> it = set.iterator();
    T first = null;
    if (it.hasNext()) {
      first = it.next();
    }
    return first;
  }


  public static <T> T firstElement(List<T> list) {
    if (isEmpty(list)) {
      return null;
    }
    return list.get(0);
  }


  public static <T> T lastElement(Set<T> set) {
    if (isEmpty(set)) {
      return null;
    }
    if (set instanceof SortedSet) {
      return ((SortedSet<T>) set).last();
    }

    // Full iteration necessary...
    Iterator<T> it = set.iterator();
    T last = null;
    while (it.hasNext()) {
      last = it.next();
    }
    return last;
  }


  public static <T> T lastElement(List<T> list) {
    if (isEmpty(list)) {
      return null;
    }
    return list.get(list.size() - 1);
  }

  public static <A, E extends A> A[] toArray(Enumeration<E> enumeration, A[] array) {
    ArrayList<A> elements = new ArrayList<>();
    while (enumeration.hasMoreElements()) {
      elements.add(enumeration.nextElement());
    }
    return elements.toArray(array);
  }

  public static <E> Iterator<E> toIterator(Enumeration<E> enumeration) {
    return (enumeration != null ? new EnumerationIterator<>(enumeration)
        : Collections.emptyIterator());
  }

  private static class EnumerationIterator<E> implements Iterator<E> {

    private final Enumeration<E> enumeration;

    public EnumerationIterator(Enumeration<E> enumeration) {
      this.enumeration = enumeration;
    }

    @Override
    public boolean hasNext() {
      return this.enumeration.hasMoreElements();
    }

    @Override
    public E next() {
      return this.enumeration.nextElement();
    }

    @Override
    public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("Not supported");
    }
  }

  @FunctionalInterface
  public interface HashCallback<T> {

    int computeHashCode(Collection<T> collection);

  }

}
