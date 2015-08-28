package com.mendeley.api.callbacks.read_position;

import com.mendeley.api.model.ReadPosition;

import java.util.Date;
import java.util.List;

public class ReadPositionList {
    public final List<ReadPosition> readPositions;
    public final Date serverDate;

    public ReadPositionList(List<ReadPosition> readPositions, Date serverDate) {
        this.readPositions = readPositions;
        this.serverDate = serverDate;
    }
}
