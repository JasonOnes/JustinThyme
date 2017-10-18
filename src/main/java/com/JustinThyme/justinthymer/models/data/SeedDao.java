package com.JustinThyme.justinthymer.models.data;

import com.JustinThyme.justinthymer.models.forms.Seed;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;


@Repository
@Transactional
public interface SeedDao extends CrudRepository<Seed, Integer> {

    List<Seed> findByArea(Seed.Area area);
    /*
    {
        return SeedDao.query.filter_by(area=area).find_all();
    }
*/
    List<Seed> findByName(String name);

    List<Seed> findBySeason(Seed.Season season);

}
