package backend_central.backend_central.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutResponse {
    private boolean ok;
    private Long institucionId;
    private String loginUrl;
}
