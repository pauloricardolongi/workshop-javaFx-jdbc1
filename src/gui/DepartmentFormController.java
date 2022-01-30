package gui;

import java.net.URL;
import java.util.ResourceBundle;

import db.DbException;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Department;
import model.services.DepartmentService;

public class DepartmentFormController implements Initializable{

	private Department entity;
	
	private DepartmentService service;
	
	@FXML
	private TextField txId;
	
	@FXML
	private TextField txName;
	
	@FXML
	private Label labelErroName;
	
	@FXML
	private Button btSave;
	
	@FXML
	private Button btCancel;
	
	
	public void setDepartment(Department entity) {
		this.entity = entity;
	}
	
	public void setDepartmentService(DepartmentService service) {
		this.service = service;
	}
	
	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("Entity está nulo");
		}
		if (service == null) {
			throw new IllegalStateException("Service está nulo");
		}
		try {
		     entity = getFormData();
		     service.saveOrUpdate(entity);
		     Utils.currentStage(event).close();
		}
		catch (DbException e) {
			Alerts.showAlert("Erro saving objcect", null, e.getMessage(), AlertType.ERROR);
		}
	}
	private Department getFormData() {
		Department obj = new Department();
		obj.setId(Utils.tryParseToInt(txId.getText()));
		obj.setName(txName.getText());
		return obj;
		
	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNode();
		
	}
	private void initializeNode() {
		Constraints.setTextFieldInteger(txId);
		Constraints.setTextFieldMaxLength(txName, 30);
		
	}
	public void updateFormDate() {
	 
		if(entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		txId.setText(String.valueOf(entity.getId()));
		txName.setText(entity.getName());
		
	}

}
