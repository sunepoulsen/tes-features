package dk.sunepoulsen.tes.features.service.domains.persistence.model;

import dk.sunepoulsen.tes.jpa.model.TimestampEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
@Setter
public class ActivationEntity extends TimestampEntity {

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "datetime", nullable = false)
    private ZonedDateTime dateTime;

}
