package service;

import entities.enums.ContainerType;

/**
 * A simple, immutable data record to hold a valid container option.
 * It pairs a potential ContainerType with the capacity for a specific BoxType.
 */
public record ContainerOption(ContainerType containerType, int capacity) {
}