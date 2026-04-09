package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IssueImageResponse {
    private Integer id;
    private Integer issueId;
    private String filePath;
    private String downloadUrl;
    private LocalDateTime createdAt;
}