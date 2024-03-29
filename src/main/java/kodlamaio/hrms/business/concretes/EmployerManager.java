package kodlamaio.hrms.business.concretes;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import kodlamaio.hrms.business.abstracts.EmailActivationService;
import kodlamaio.hrms.business.abstracts.EmployerService;
import kodlamaio.hrms.business.abstracts.UserService;
import kodlamaio.hrms.business.constants.Messages;
import kodlamaio.hrms.core.utilities.business.BusinessRules;
import kodlamaio.hrms.core.utilities.results.DataResult;
import kodlamaio.hrms.core.utilities.results.ErrorResult;
import kodlamaio.hrms.core.utilities.results.Result;
import kodlamaio.hrms.core.utilities.results.SuccessDataResult;
import kodlamaio.hrms.core.utilities.results.SuccessResult;
import kodlamaio.hrms.dataAccess.abstracts.EmployerDao;
import kodlamaio.hrms.entities.concretes.EmailActivation;
import kodlamaio.hrms.entities.concretes.Employer;
import kodlamaio.hrms.entities.concretes.User;

@Service
public class EmployerManager implements EmployerService{

	private EmployerDao employerDao;
	private UserService userService;
	private EmailActivationService emailActivationService;
	
	@Autowired
	public EmployerManager(EmployerDao employerDao, UserService userService, EmailActivationService emailActivationService) {
		this.employerDao = employerDao;
		this.emailActivationService = emailActivationService;
		this.userService = userService;
	}
	
	@Override
	public DataResult<Page<Employer>> getAll(int pageNo, int pageSize) {
		Pageable pageable = PageRequest.of(pageNo-1, pageSize);
		return new SuccessDataResult<Page<Employer>>(this.employerDao.findAll(pageable), "Data listelendi");
	
	}

	@Override
	public Result add(Employer employer) {
		
		Result result = BusinessRules.run(companyNameChecker(employer), webAddressChecker(employer),
				        emailChecker(employer), passwordChecker(employer),
				        isRealEmail(employer), isRealPhoneNumber(employer),
				        isEmailExist(employer)
				        );
		
		if(!result.isSuccess()) {
			return new ErrorResult(result.getMessage());
		}
		
		User user = this.userService.add(employer);
		this.emailActivationService.generateCode(new EmailActivation(),user.getId());
		return new SuccessDataResult<Employer>(this.employerDao.save(employer),"İş Veren Hesabı Eklendi , Doğrulama Kodu Gönderildi:" + employer.getId());
				
	}
	
	@Override
	public Result update(Employer employer) {
		
		Employer updatedEmployer = employerDao.getOne(employer.getId());
        employer.setActivated(!employer.isActivated());
        updatedEmployer.setUpdatedData(employer);

        employerDao.save(updatedEmployer);
        return new SuccessResult("Güncelleme başarılı, Aktivasyon için onay bekleniyor");
	}
	
	@Override
	public Result updateConfirm(int userId) {
		
		Employer employer = employerDao.getOne(userId);
        
		if (!Objects.isNull(employer.getUpdatedData())) {
            Employer updatedData = employer.getUpdatedData();
            employerDao.save(updatedData);
            return new SuccessResult("Güncellemeler onaylandi");
        }
		
        employer.setActivated(!employer.isActivated());
        employerDao.save(employer);
        return new SuccessResult("Kullanıcı onaylandi");
	}
	
	@Override
	public Result updateChangeActive(int userId) {
        employerDao.updateChangeActive(userId);
		return new SuccessResult("Kullanıcı onaylandi");
	}
	
	@Override
	public Result updateChangeFalse(int userId) {
        employerDao.updateChangeFalse(userId);
		return new SuccessResult("Kullanıcı onaylanmadi");
	}
	
	@Override
	public DataResult<List<Employer>> getAllByNotActive(int pageNo, int pageSize) {
		Pageable pageable = PageRequest.of(pageNo-1, pageSize);
		return new SuccessDataResult<List<Employer>>(this.employerDao.getAllByNotActive(pageable));
	}
	
	@Override
	public DataResult<List<Employer>> getAllByIsActive(int pageNo, int pageSize) {
		Pageable pageable = PageRequest.of(pageNo-1, pageSize);
		return new SuccessDataResult<List<Employer>>(this.employerDao.getAllByIsActive(pageable));
	}
	
	@Override
	public DataResult<Employer> getById(int id) {
		return new SuccessDataResult<Employer>(this.employerDao.getOne(id), "Employer detaylari getirildi");
	}

	@Override
	public DataResult<List<Employer>> getByUpdatedDataNotNull() {
        return new SuccessDataResult<List<Employer>>(employerDao.getByUpdatedDataNotNull());

	}
	
	
	
	
	private Result companyNameChecker(Employer employer) {
		
		if(employer.getCompanyName() == null) {
			return new ErrorResult(Messages.requiredCompanyName);
		}
		return new SuccessResult();
	}
	
   private Result webAddressChecker(Employer employer) {
		
		if(employer.getWebAddress() == null) {
			return new ErrorResult(Messages.requiredWebAddress);
		}
		return new SuccessResult();
	}
   
   private Result emailChecker(Employer employer) {
		
		if(employer.getEmail() == null) {
			return new ErrorResult(Messages.requiredEmail);
		}
		return new SuccessResult();
	}
   
   private Result passwordChecker(Employer employer) {
		
		if(employer.getPassword() == null) {
			return new ErrorResult(Messages.requiredPassword);
		}
		return new SuccessResult();
	}
   
   private Result isRealEmail(Employer employer) {
		 String regex = "^(.+)@(.+)$";
	     Pattern pattern = Pattern.compile(regex);
	     Matcher matcher = pattern.matcher(employer.getEmail());
	     if(!matcher.matches()) {
	    	 return new ErrorResult(Messages.notRealEmail);
	     }
	     else if (!employer.getEmail().contains(employer.getWebAddress())) {
				return new ErrorResult();
			}
	     return new SuccessResult();
	     
	}
   
   private Result isRealPhoneNumber(Employer employer) {
		String patterns = "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$"
				+ "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?){2}\\d{3}$"
				+ "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?)(\\d{2}[ ]?){2}\\d{2}$";

		Pattern pattern = Pattern.compile(patterns);
		Matcher matcher = pattern.matcher(employer.getPhoneNumber());
		if (!matcher.matches()) {
			return new ErrorResult(Messages.invalidPhoneNumber);
		}
		return new SuccessResult();
	}
   
   private Result isEmailExist(Employer employer) {
		
		if(employerDao.findAllByEmail(employer.getEmail()).stream().count() !=0) {
			return new ErrorResult(Messages.alreadyRegisteredEmail);
		}
		return new SuccessResult();
	}
 
}
