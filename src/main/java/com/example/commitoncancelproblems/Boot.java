package com.example.commitoncancelproblems;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author rpuch
 */
@Document
@Data
public class Boot {
    private String kind;
}
