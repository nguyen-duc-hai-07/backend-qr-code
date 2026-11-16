package org.oplearn.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.oplearn.project.entity.base.BaseEntity;

@Entity
@Table(name = "store_tables")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreTable extends BaseEntity {

  @Column(name = "zone", length = 50, nullable = false)
  private String zone;

  @Column(name = "location_name", length = 150, nullable = false)
  private String locationName;

  @Column(name = "qr_token", length = 255, nullable = false, unique = true)
  private String qrToken;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = Boolean.TRUE;
}
