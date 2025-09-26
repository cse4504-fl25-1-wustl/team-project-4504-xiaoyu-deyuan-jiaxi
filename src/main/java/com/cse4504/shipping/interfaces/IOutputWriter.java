package com.cse4504.shipping.interfaces;

import com.cse4504.shipping.domain.Shipment;
import java.io.IOException;

public interface IOutputWriter {
    void writeOutput(String outputFile, Shipment shipment) throws IOException;
}
