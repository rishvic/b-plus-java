/*
 * Copyright 2023 Rishvic Pushpakaran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.rishvic.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;

public class BPlusTree<E extends Comparable<E>> {

  private final int bf;
  private Node<E> root;

  /** Default constructor. Sets the branching factor to 3. */
  public BPlusTree() {
    bf = 3;
    root = new Node<>(null, null, true);
  }

  /**
   * Returns whether the B+ Tree is empty.
   *
   * @return Whether the B+ Tree is empty.
   */
  public boolean isEmpty() {
    return root.items.isEmpty();
  }

  /** Clears the B+ Tree. */
  public void clear() {
    root = new Node<>(null, null, true);
  }

  /**
   * Returns the first element in the B+ Tree. Since the elements are stored in ascending order,
   * returns the smallest element of the set. If the tree is empty, returns {@code null}.
   *
   * @return The first element of the set. {@code null} if the tree is empty.
   */
  public E first() {
    return root.first();
  }

  /**
   * Returns the last element in the B+ Tree. Since the elements are stored in ascending order,
   * returns the largest element of the set. If the tree is empty, returns {@code null}.
   *
   * @return The first element of the set. {@code null} if the tree is empty.
   */
  public E last() {
    return root.last();
  }

  /**
   * Returns whether the B+ Tree contains the element or not.
   *
   * @param element The element to check.
   * @return {@code true} if element is present, otherwise {@code false}.
   */
  public boolean contains(E element) {
    return containsRecursive(root, element);
  }

  /**
   * Constructor for the class, where we can specify the branching factor.
   *
   * @param bf The branching factor of the tree.
   * @throws IllegalArgumentException If the specified branching factor is less than 3.
   */
  public BPlusTree(int bf) throws IllegalArgumentException {
    if (bf < 3) throw new IllegalArgumentException("Branching factor must be at least 3");
    this.bf = bf;
    this.root = new Node<>(null, null, true);
  }

  /**
   * Adds the element to the B+ Tree.
   *
   * @param element The element to add to the tree.
   */
  public void add(E element) {
    addRecursive(root, element);
    if (root.items.size() <= maxItemCount(root)) return;

    Pair<E, Node<E>> splitRes = root.splitMid();
    assert splitRes != null : "root.splitMid() returned `null`";
    E midElement = splitRes.getLeft();
    Node<E> splitRoot = splitRes.getRight();

    Node<E> newRoot = new Node<>(null, null, false);
    newRoot.items.add(midElement);
    newRoot.children.add(root);
    newRoot.children.add(splitRoot);

    root = newRoot;
  }

  /**
   * Removes an element from the B+ Tree.
   *
   * @param element The element to remove from the tree.
   * @return {@code true} if the element was found & removed, {@code false} if the element was not
   *     in the tree.
   */
  public boolean remove(E element) {
    boolean removed = removeRecursive(root, element);
    if (root.items.isEmpty() && !root.isLeaf()) root = root.children.get(0);
    return removed;
  }

  private boolean containsRecursive(Node<E> node, E element) {
    if (node.isLeaf()) return node.items.contains(element);
    int at;
    for (at = 0; at < node.items.size(); at++) {
      if (node.items.get(at).compareTo(element) > 0) break;
    }

    return containsRecursive(node.children.get(at), element);
  }

  private void addRecursive(Node<E> node, E element) {
    int at;
    for (at = 0; at < node.items.size(); at++) {
      if (node.items.get(at).compareTo(element) > 0) break;
    }

    if (node.isLeaf()) {
      if (!node.items.contains(element)) node.items.add(at, element);
      return;
    }

    Node<E> child = node.children.get(at);
    addRecursive(child, element);
    if (child.items.size() <= maxItemCount(child)) return;

    Pair<E, Node<E>> splitRes = child.splitMid();
    assert splitRes != null : "child.splitMid() returned `null`";
    E midElement = splitRes.getLeft();
    Node<E> splitChild = splitRes.getRight();

    node.items.add(at, midElement);
    node.children.add(at + 1, splitChild);
  }

  private boolean removeRecursive(Node<E> node, E element) {
    if (node.isLeaf()) {
      return node.items.remove(element);
    }

    int at;
    for (at = 0; at < node.items.size(); at++) {
      if (node.items.get(at).compareTo(element) > 0) break;
    }

    Node<E> child = node.children.get(at);
    boolean removed = removeRecursive(child, element);
    if (child.items.size() >= minItemCount(child)) return removed;

    Node<E> sibling;
    if (at >= 1
        && node.children.get(at - 1).items.size() >= minItemCount(node.children.get(at - 1)) + 1) {
      sibling = node.children.get(at - 1);
      E siblingItem = sibling.items.get(sibling.items.size() - 1);
      sibling.items.remove(sibling.items.size() - 1);

      E nodeItem = node.items.get(at - 1);
      node.items.set(at - 1, siblingItem);
      child.items.add(0, nodeItem);

      if (!child.isLeaf()) {
        Node<E> siblingChild = sibling.children.get(sibling.children.size() - 1);
        sibling.children.remove(sibling.children.size() - 1);
        child.children.add(0, siblingChild);
      }

      node.items.set(at - 1, child.first());
      return removed;
    }

    if (at < node.children.size() - 1
        && node.children.get(at + 1).items.size() >= minItemCount(node.children.get(at + 1)) + 1) {
      sibling = node.children.get(at + 1);
      E siblingItem = sibling.items.get(0);
      sibling.items.remove(0);

      E nodeItem = node.items.get(at);
      node.items.set(at, siblingItem);
      child.items.add(nodeItem);

      if (!child.isLeaf()) {
        Node<E> siblingChild = sibling.children.get(0);
        sibling.children.remove(0);
        child.children.add(siblingChild);
      }

      node.items.set(at, sibling.first());
      return removed;
    }

    if (at == node.children.size() - 1) at--;
    child = node.children.get(at);
    sibling = node.children.get(at + 1);

    if (!child.isLeaf()) {
      child.items.add(node.items.get(at));
      child.children.addAll(sibling.children);
    }
    child.items.addAll(sibling.items);

    child.next = sibling.next;
    if (child.next != null) child.next.prev = child;

    node.items.remove(at);
    node.children.remove(at + 1);

    return removed;
  }

  private <T extends Comparable<T>> int minChildCount(Node<T> node) {
    return node.isLeaf() ? 0 : (bf + 1) / 2;
  }

  private <T extends Comparable<T>> int minItemCount(Node<T> node) {
    return node.isLeaf() ? (bf + 1) / 2 : minChildCount(node) - 1;
  }

  private <T extends Comparable<T>> int maxChildCount(Node<T> node) {
    return node.isLeaf() ? 0 : bf;
  }

  private <T extends Comparable<T>> int maxItemCount(Node<T> node) {
    return bf - 1;
  }

  /**
   * Returns a pretty-printed version of the tree with a specified prefix for each line.
   *
   * @param prefix The prefix to add to each line.
   * @return A string with representation of the tree.
   */
  public String toPrettyString(String prefix) {
    StringBuilder sb = new StringBuilder();
    sb.append(prefix).append(root.items).append('\n');
    root.toPrettyStringChildren(sb, prefix);
    return sb.toString();
  }

  /**
   * Pretty-print the tree with no prefix.
   *
   * @return A string with pretty-print of tree, without any prefix.
   */
  public String toPrettyString() {
    return toPrettyString("");
  }

  private static class Node<E extends Comparable<E>> {
    List<E> items;
    Node<E> next, prev;
    List<Node<E>> children;

    Node(Node<E> prev, Node<E> next, boolean isLeaf) {
      this.items = new ArrayList<>();
      this.next = next;
      this.prev = prev;
      this.children = isLeaf ? null : new ArrayList<Node<E>>();
    }

    boolean isLeaf() {
      return children == null;
    }

    Pair<E, Node<E>> splitMid() {
      if (items.size() < 2) return null;

      int midIndex = items.size() / 2;
      E midElement = items.get(midIndex);

      Node<E> splitNode = new Node<>(this, next, isLeaf());
      this.next = splitNode;
      if (splitNode.next != null) splitNode.next.prev = splitNode;

      List<E> splitItems = items.subList(isLeaf() ? midIndex : midIndex + 1, items.size());
      splitNode.items.addAll(splitItems);
      splitItems.clear();
      if (!isLeaf()) items.remove(items.size() - 1);

      if (!isLeaf()) {
        List<Node<E>> splitChildren = children.subList(midIndex + 1, children.size());
        splitNode.children.addAll(splitChildren);
        splitChildren.clear();
      }

      return Pair.of(midElement, splitNode);
    }

    E first() {
      if (items.isEmpty()) return null;
      if (isLeaf()) return items.get(0);
      return children.get(0).first();
    }

    E last() {
      if (items.isEmpty()) return null;
      if (isLeaf()) return items.get(items.size() - 1);
      return children.get(children.size() - 1).last();
    }

    void toPrettyStringChildren(StringBuilder sb, String prefix) {
      if (isLeaf()) return;

      for (int index = 0; index < children.size(); index++) {
        boolean isLastChild = index == children.size() - 1;
        sb.append(prefix)
            .append(isLastChild ? "└─" : "├─")
            .append(children.get(index).items)
            .append('\n');
        children
            .get(index)
            .toPrettyStringChildren(sb, isLastChild ? prefix + "   " : prefix + "│  ");
      }
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("items", items)
          .append("children", children)
          .toString();
    }
  }
}
