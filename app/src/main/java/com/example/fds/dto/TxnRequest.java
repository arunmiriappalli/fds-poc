
package com.example.fds.dto;

import jakarta.validation.constraints.*;

public class TxnRequest {
  @NotBlank
  public String txn_id;
  @NotBlank
  public String ts;
  @NotBlank
  public String card_id;
  @NotNull
  public Double amount;
  @NotNull
  public Double lat;
  @NotNull
  public Double lon;
  @NotBlank
  public String device_id;
}
