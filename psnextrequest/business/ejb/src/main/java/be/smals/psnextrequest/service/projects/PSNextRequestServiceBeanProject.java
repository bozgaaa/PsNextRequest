package be.smals.psnextrequest.service.projects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import be.smals.psnextrequest.entity.Project;
import be.smals.psnextrequest.entity.ProjectTeam;
import be.smals.psnextrequest.entity.User;
import be.smals.psnextrequest.service.exception.PSNextRequestServiceException;
import be.smals.psnextrequest.service.users.PSNextRequestServiceRemoteUser;

/**
 * PSNextRequest service projects implementation (EJB).
 * 
 * @author AndreiBozga
 * 
 * @since
 * 
 */
@Stateless (name = "PSNextRequestServiceBeanProject")
public class PSNextRequestServiceBeanProject implements PSNextRequestServiceRemoteProject{
	
	@PersistenceContext(unitName = "PSNextRequestUnit") 
	private EntityManager em;
	
	private Logger logger = Logger.getLogger("UserLogger");
	FileHandler fh = setLogger();

	@EJB 
    private PSNextRequestServiceRemoteUser serviceUser;
	
    protected void flush() {
        em.flush();
    }
    
	/* ---------------- Project -------------------- */
    /**
     * @see be.smals.psnextrequest.service.PSNextRequestService#createProject(be.smals.psnextrequest.entity.Project)
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Project createProject(List<User> membersTeam, Project project) throws PSNextRequestServiceException {
		try{
			em.getTransaction().begin();
			//create project team
			ProjectTeam prjTeam = createProjectTeam(membersTeam, project);

    		project.setProjectCreationDate(new Date());
    		project.setProjectTeam(prjTeam);
			em.persist(project);
			em.flush();
			logger.log(Level.INFO, "Nouveau projet cr��: " + project.getProjectName());
    		em.getTransaction().commit();
    		return project;
    		
		} catch (PersistenceException e) {
			if(em.getTransaction().isActive()){
				em.getTransaction().rollback();
			}
			if(e.getMessage().contains("Error Code: 1062")){
				throw new PSNextRequestServiceException("Le nom du projet existe d�j�", e);
			} else if(e.getMessage().contains("Error Code: 1048")){
				throw new PSNextRequestServiceException("Le projet n'a pas �t� s�lection�", e);
			} else {
				throw new PSNextRequestServiceException("Contrainte violation exception", e);
			}
        } catch (Exception e) {
        	if(em.getTransaction().isActive()){
        		em.getTransaction().rollback();
			}
            throw new PSNextRequestServiceException("Project cannot be created", e);
        }   
    }
    
    /**
     * @see be.smals.psnextrequest.service.PSNextRequestService#updateProject(List<User>, be.smals.psnextrequest.entity.Project)
     */
    @Override
    public Project updateProject(List<User> users, Project project) throws PSNextRequestServiceException {
    	Project existingProject = null;
        try {
        	//Find project
            if (project.getProjectId() != null) {
                existingProject = em.find(Project.class, project.getProjectId());
            } 
        } catch(NoResultException e){
        	throw new PSNextRequestServiceException("Project cannot be found");
        } catch (Exception e) {
        	throw new PSNextRequestServiceException("An unexpected error has occurred: " + e.getMessage());
        }
        if (existingProject != null) {
        	try{
        		//update project & project team
        		project.setProjectLastUpdateDate(new Date());
//            	updateProjectTeam(users, project);
            	em.getTransaction().begin();
                em.merge(project);
                em.flush();
                logger.log(Level.INFO, "Le projet: " + project.getProjectName() + " a �t� mis � jour.");
                em.getTransaction().commit();
                return project;
        	}
        	catch (Exception e){
        		throw new PSNextRequestServiceException("An unexpected error has occurred: " + e.getMessage());
        	}
        }
        else
        {
        	throw new PSNextRequestServiceException("Project cannot be found");
        }
    }
    
    /**
     * @see be.smals.psnextrequest.service.PSNextRequestService#disableProject(be.smals.psnextrequest.entity.Project)
     */
    @Override
    public void disableProject(Project project) throws PSNextRequestServiceException {
        try {
        	em.getTransaction().begin();
        	//disable project by status 3
        	project.setProjectStatus(3);
        	em.merge(project);
        	em.flush();
        	em.getTransaction().commit();
        }catch (Exception e){
	        if(em.getTransaction().isActive()){
				em.getTransaction().rollback();
			}
        	throw new PSNextRequestServiceException("An unexpected error has occurred: " + e.getMessage());
        }
    }
    
    /**
     * @see be.smals.psnextrequest.service.PSNextRequestService#getProjectById(java.lang.Long)
     */
    @Override
    public Project getProjectById(Long projectId) throws PSNextRequestServiceException {
    	try{
    		return em.find(Project.class, projectId);
    	} catch (NoResultException e){
    		throw new PSNextRequestServiceException("Project cannot be found");
    	} catch (Exception e) {
    		throw new PSNextRequestServiceException("An unexpected error has occurred: " + e.getMessage());
        }
    }
    
    /**
     * @see be.smals.psnextrequest.service.PSNextRequestService#getProjectByTeam(be.smals.psnextrequest.entity.projectTeam)
     */
    @Override
    public Project getProjectByTeam(ProjectTeam projectTeam) throws PSNextRequestServiceException{
    	try{
    		Project project = new Project();
			Query query = em.createQuery("SELECT p FROM Project p WHERE p.projectTeam = :projectTeam");
			query.setParameter("projectTeam", projectTeam);
			project = (Project) query.getSingleResult();
			return project;
		} catch (EntityNotFoundException e){
			throw new PSNextRequestServiceException("Project cannot be found");
		}
    }
    
    /**
     * @see be.smals.psnextrequest.service.PSNextRequestService#getAllProjects()
     */
    @Override
    public List<Project> getAllProjects() throws PSNextRequestServiceException {
    	try{
    		Query createQuery = em.createQuery("SELECT p FROM Project p WHERE p.projectStatus != 3 ");
            @SuppressWarnings("unchecked")
			List<Project> resultList = (List<Project>) createQuery.getResultList();
            return resultList;
    	} catch(Exception e){
    		throw new PSNextRequestServiceException("An unexpected error has occurred: " + e.getMessage());
    	}
    }
    
    
    /* ---------------- Project Team -------------------- */
    /**
     * @see be.smals.psnextrequest.service.PSNextRequestService#createProjectTeam()
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public ProjectTeam createProjectTeam(List<User> membersTeam, Project project)throws PSNextRequestServiceException{
    	try{
        	ProjectTeam prjTeam = new ProjectTeam();
        	prjTeam.setProjectTeamName(project.getProjectName());
        	//prjTeam.setUsers(membersTeam);
        	em.persist(prjTeam);
        	//Transaction many-to-many ProjectTeam_Users
        	for(User user : membersTeam){
        		user.getTeams().add(prjTeam);
        		em.merge(user);
        	}
        	
        	em.flush();
    		logger.log(Level.INFO, "Nouvelle �quipe de projet cr��e: " + prjTeam.getProjectTeamName());
    		return prjTeam;
    	}catch(Exception e) {
			throw new PSNextRequestServiceException(e.getMessage());
		}
    }
    
    /**
     * @see be.smals.psnextrequest.service.PSNextRequestService#createProjectTeam()
     */
    @Override
    public void updateProjectTeam(List<User> membersTeam, Project project) throws PSNextRequestServiceException{
    	em.getTransaction().begin();
    	ProjectTeam existingProjectTeam = null;
        try {
            if (project.getProjectTeam().getProjectTeamId() != null) {
            	existingProjectTeam = em.find(ProjectTeam.class, project.getProjectTeam().getProjectTeamId());
            } 
        } catch(NoResultException e){
        	throw new PSNextRequestServiceException("Project team cannot be found");
        } catch (Exception e) {
        	throw new PSNextRequestServiceException("An unexpected error has occurred: " + e.getMessage());
        }
        if (existingProjectTeam != null) {
        	try{   		
//            	for(User member : membersTeam){
//            		for(User user: project.getProjectTeam().getUsers()){
//            			if(!user.getUserDistinguishedName().equals(member.getUserDistinguishedName())){
////            				membersTeam.remove(member);
//            				existingProjectTeam.addUser(member);
//            				user.getTeams().add(existingProjectTeam);
//            			}
//            		}
//            	}
            	
//        		//existingProjectTeam.getUsers().clear();
//        		for(Iterator<User> itr = existingProjectTeam.getUsers().iterator(); itr.hasNext();)  
//                {  
//        			itr.remove();
//        			
////                    User user = itr.next();  
////                    if("2".equals(user))  
////                    {  
////                        itr.remove();  
////                    }  
//                }  
                
//        		for(User existingMembers : existingProjectTeam.getUsers()){
//        			existingProjectTeam.getUsers().remove(existingMembers);
//                }

//        		for(int i=0; i<=existingProjectTeam.getUsers().size(); ++i){
//        			existingProjectTeam.getUsers().remove(i);
//        			--i;
//        		}
//        		em.merge(existingProjectTeam);
//                em.flush();
//                
//                for(User member : membersTeam){
//                	member.addTeam(existingProjectTeam);
//                	existingProjectTeam.addUser(member);
//                }
//            	
        		
        		
        		
        		List<Integer> indexs = new ArrayList<Integer>();
        		for(User existingMember : existingProjectTeam.getUsers()){
        			for(User newMember : membersTeam){
        				if(existingMember.getUserDistinguishedName().equals(newMember.getUserDistinguishedName())){
        					//source.remove(source.indexOf(sourceUser));
        					indexs.add(existingProjectTeam.getUsers().indexOf(existingMember));
        					System.out.println("Is removed: " + existingMember.getUserDistinguishedName());
        				}
        			}
        		}
        		
        		//setSourceFiltered(existingProjectTeam.getUsers());
        		int nbRemoved=0;
        		for(int index : indexs){
        			System.out.println("remove index: " + index);
        			existingProjectTeam.getUsers().remove(index-nbRemoved);		
        			nbRemoved++;
        			System.out.println("isRemoved index: " + index);
        		}
        		
                
//        		for(User newMember : membersTeam){
//        			existingProjectTeam.addUser(newMember);
//        			newMember.addTeam(existingProjectTeam);
//        		}
        		
        		
        		
        		
                em.merge(existingProjectTeam);
                em.flush();
                logger.log(Level.INFO, "L' �quipe de projet: " + project.getProjectName() + " a �t� mis � jour.");
                em.getTransaction().commit();
        	}
        	catch (Exception e){
        		throw new PSNextRequestServiceException("An unexpected error has occurred: " + e.getMessage());
        	}
        } else {
        	throw new PSNextRequestServiceException("Project cannot be found");
        }
        
    	
    }
    
    /**
     * @see be.smals.psnextrequest.service.PSNextRequestServiceProject#getProjectTeamByName()
     */
    @Override
    public ProjectTeam getProjectTeamByName(String name) throws PSNextRequestServiceException{
    	try{
    		ProjectTeam projectTeam = new ProjectTeam();
			Query query = em.createQuery("SELECT p FROM ProjectTeam p WHERE p.projectTeam = :projectTeam");
			query.setParameter("projectTeam", projectTeam);
			projectTeam = (ProjectTeam) query.getSingleResult();
			return projectTeam;
		} catch (EntityNotFoundException e){
			throw new PSNextRequestServiceException("Project team cannot be found");
		}
    }
    
    //Initialiser et formater un fichier log 
    public FileHandler setLogger(){
		try {
	      // This block configure the logger with handler and formatter
	      fh = new FileHandler("c:\\PsNextLogs\\UserLogFile.log", true);
	      logger.addHandler(fh);
	      logger.setLevel(Level.ALL);
	      SimpleFormatter formatter = new SimpleFormatter();
	      fh.setFormatter(formatter);
	    } catch (SecurityException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
		return fh;
	}

}