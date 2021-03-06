package kodlamaio.hrms.business.concretes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import kodlamaio.hrms.business.abstracts.FavoriteJobAdvertService;
import kodlamaio.hrms.core.utilities.results.DataResult;
import kodlamaio.hrms.core.utilities.results.ErrorResult;
import kodlamaio.hrms.core.utilities.results.Result;
import kodlamaio.hrms.core.utilities.results.SuccessDataResult;
import kodlamaio.hrms.core.utilities.results.SuccessResult;
import kodlamaio.hrms.dataAccess.abstracts.CandidateDao;
import kodlamaio.hrms.dataAccess.abstracts.FavoriteJobAdvertDao;
import kodlamaio.hrms.dataAccess.abstracts.JobAdvertDao;
import kodlamaio.hrms.entities.concretes.FavoriteJobAdvert;

@Service
public class FavoriteJobAdvertManager implements FavoriteJobAdvertService{
	
	private FavoriteJobAdvertDao favoriteJobAdvertDao;
	private CandidateDao candidateDao;
    private JobAdvertDao jobAdvertDao;

	@Autowired
	public FavoriteJobAdvertManager(FavoriteJobAdvertDao favoriteJobAdvertDao, CandidateDao candidateDao, JobAdvertDao jobAdvertDao) {
		super();
		this.favoriteJobAdvertDao = favoriteJobAdvertDao;
		this.candidateDao = candidateDao;
		this.jobAdvertDao = jobAdvertDao;
	}

	
	@Override
	public DataResult<Page<FavoriteJobAdvert>> getAll(int pageNo, int pageSize) {
		Pageable pageable = PageRequest.of(pageNo-1, pageSize);
		return new SuccessDataResult<Page<FavoriteJobAdvert>>(favoriteJobAdvertDao.findAll(pageable),"Favoriler listesi");
	}
	
	@Override
	public DataResult<FavoriteJobAdvert> getById(int id) {	
		return new SuccessDataResult<FavoriteJobAdvert>(favoriteJobAdvertDao.findById(id).get());
	}

	
	@Override
	public DataResult<Page<FavoriteJobAdvert>> getAllByCandidateId(int candidateId, int pageNo, int pageSize) {
		Pageable pageable = PageRequest.of(pageNo-1, pageSize);
		return new SuccessDataResult<Page<FavoriteJobAdvert>>(favoriteJobAdvertDao.getAllByCandidate_Id(candidateId, pageable), "???? arayana g??re favori i?? ilanlar?? getirme i??lemi ba??ar??l??!");
	}

	@Override
	public Result add(FavoriteJobAdvert favoriteJobAdvert) {
		
		if(!this.candidateDao.existsById(favoriteJobAdvert.getCandidate().getId())){
            
			return new ErrorResult("B??yle bir kullan??c?? yok");
        }
		else if(!this.jobAdvertDao.existsById(favoriteJobAdvert.getJobAdvert().getId())){
            
			return new ErrorResult("B??yle bir ilan yok");
        }
		else if(this.favoriteJobAdvertDao.existsByCandidate_IdAndJobAdvert_Id(favoriteJobAdvert.getCandidate().getId(), favoriteJobAdvert.getJobAdvert().getId())){
            
			return new ErrorResult("Bu ilan zaten favorilerinizde");
        }
		
		this.favoriteJobAdvertDao.save(favoriteJobAdvert);
		return new SuccessResult("Favorilere Ekleme i??lemi ba??ar??l??!");
	}

	@Override
	public Result delete(int candidateId, int jobAdvertId) {
		if (!favoriteJobAdvertDao.existsByCandidate_IdAndJobAdvert_Id(candidateId, jobAdvertId)) {
			return new ErrorResult("Favori i?? ilan?? bulunamad??.");
		}
		
		favoriteJobAdvertDao.deleteByCandidate_IdAndJobAdvert_Id(candidateId, jobAdvertId);
		return new SuccessResult("Favorilerden kald??rma i??lemi ba??ar??l??!");
	}

}
