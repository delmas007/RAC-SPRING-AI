package angaman.cedrick.rag_openai.Repository;

import angaman.cedrick.rag_openai.Model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, String>{
    Optional<Utilisateur> findByUsername(String username);

    Optional<Utilisateur> findByEmail(String email);

}
