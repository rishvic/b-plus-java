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
    if (root.items.size() < bf) return;

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

  private boolean containsRecursive(Node<E> node, E element) {
    if (node.isLeaf()) return node.items.contains(element);
    int at;
    for (at = 0; at < node.items.size(); at++) {
      if (node.items.get(at).compareTo(element) > 0) break;
    }

    if (at < node.items.size() && node.items.equals(element)) return true;
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
    if (child.items.size() < bf) return;

    Pair<E, Node<E>> splitRes = child.splitMid();
    assert splitRes != null : "child.splitMid() returned `null`";
    E midElement = splitRes.getLeft();
    Node<E> splitChild = splitRes.getRight();

    node.items.add(at, midElement);
    node.children.add(at + 1, splitChild);
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

    private void toPrettyStringChildren(StringBuilder sb, String prefix) {
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
