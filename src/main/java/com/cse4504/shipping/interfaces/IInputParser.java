package com.cse4504.shipping.interfaces;

import com.cse4504.shipping.domain.Item;
import java.io.IOException;
import java.util.List;

public interface IInputParser {
    List<Item> parseInput(String inputFile) throws IOException;
}
