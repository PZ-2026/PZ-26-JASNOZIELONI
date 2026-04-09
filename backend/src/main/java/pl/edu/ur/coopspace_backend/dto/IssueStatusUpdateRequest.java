package pl.edu.ur.coopspace_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.ur.coopspace_backend.entity.IssueStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IssueStatusUpdateRequest {
    private IssueStatus status;
}
