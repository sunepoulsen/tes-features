package dk.sunepoulsen.tes.features.service.domains.persistence.model;

import dk.sunepoulsen.tes.jpa.model.TimestampEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@Data
@ToString(exclude = {"activations", "features"})
@EqualsAndHashCode(exclude = {"activations", "features"}, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "feature_groups")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class FeatureGroupEntity extends TimestampEntity {

    private static final String SEQUENCE_NAME = "feature_groups_id_seq";

    /**
     * Primary key.
     */
    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    @Column(name = "id")
    private Long id;

    @Column(name = "group_key", nullable = false)
    private String key;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @OneToMany(mappedBy = "featureGroup", cascade = CascadeType.ALL)
    private List<FeatureEntity> features;

    @OneToMany(mappedBy = "featureGroup", cascade = CascadeType.ALL)
    private List<FeatureGroupActivationEntity> activations;

}
