package service;

import entities.enums.BoxType;

/**
 * A simple, immutable data record to hold a valid packing option.
 * It pairs a potential BoxType with the capacity for a specific Art.
 */
public record PackingOption(BoxType boxType, int capacity) {
}