package com.dpjha.slidingnumbersplus.game

import java.util.PriorityQueue
import kotlin.math.abs

/**
 * Suggests the next tap. A weighted A* search over chain slides finds a short solution on small boards;
 * on big boards it heads for the most promising position it reached within its node budget.
 */
object Solver {
    private class Node(val tiles: ByteArray, val gap: Int, val g: Int, val h: Int, val first: Int, val prevGap: Int) {
        val f: Int get() = g * 2 + h * 3
    }

    /** Index of the cell to tap, or null when the board is already solved. */
    fun hint(rows: Int, cols: Int, tiles: IntArray, budget: Int = 60_000): Int? {
        val start = ByteArray(tiles.size) { tiles[it].toByte() }
        val startH = heuristic(start, rows, cols)
        if (startH == 0) return null
        val open = PriorityQueue<Node>(256) { a, b -> if (a.f != b.f) a.f - b.f else a.h - b.h }
        val seen = HashSet<String>()
        open.add(Node(start, tiles.indexOf(0), 0, startH, -1, -1))
        seen.add(String(start, Charsets.ISO_8859_1))
        var best: Node? = null
        var expanded = 0
        while (open.isNotEmpty() && expanded < budget) {
            val node = open.poll()!!
            expanded++
            val gr = node.gap / cols
            val gc = node.gap % cols
            for (cell in 0 until rows * cols) {
                if (cell == node.gap || cell == node.prevGap) continue
                val r = cell / cols
                val c = cell % cols
                if (r != gr && c != gc) continue
                val next = node.tiles.copyOf()
                val step = if (r == gr) (if (c > gc) 1 else -1) else (if (r > gr) cols else -cols)
                var at = node.gap
                while (at != cell) {
                    next[at] = next[at + step]
                    at += step
                }
                next[cell] = 0
                if (!seen.add(String(next, Charsets.ISO_8859_1))) continue
                val h = heuristic(next, rows, cols)
                val child = Node(next, cell, node.g + 1, h, if (node.first < 0) cell else node.first, node.gap)
                if (h == 0) return child.first
                val b = best
                if (b == null || h < b.h || (h == b.h && child.g < b.g)) best = child
                open.add(child)
            }
        }
        return best?.first
    }

    /** Manhattan distance plus a penalty for pairs that must pass each other in their home line. */
    private fun heuristic(t: ByteArray, rows: Int, cols: Int): Int {
        var sum = 0
        for (i in t.indices) {
            val v = t[i].toInt()
            if (v == 0) continue
            val home = v - 1
            val r = i / cols
            val c = i % cols
            val hr = home / cols
            val hc = home % cols
            sum += abs(r - hr) + abs(c - hc)
            if (r == hr) for (k in i + 1 until (r + 1) * cols) {
                val o = t[k].toInt() - 1
                if (o >= 0 && o / cols == r && o % cols < hc) sum += 2
            }
            if (c == hc) {
                var k = i + cols
                while (k < t.size) {
                    val o = t[k].toInt() - 1
                    if (o >= 0 && o % cols == c && o / cols < hr) sum += 2
                    k += cols
                }
            }
        }
        return sum
    }
}
