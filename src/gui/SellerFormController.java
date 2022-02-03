package gui;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	private Seller entity;

	private SellerService service;

	private DepartmentService departmentService;

	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private TextField txId;

	@FXML
	private TextField txName;

	@FXML
	private TextField txEmail;

	@FXML
	private DatePicker dpBirthDate;

	@FXML
	private TextField txBaseSalary;

	@FXML
	private ComboBox<Department> comboBoxDepartment;

	@FXML
	private Label labelErroName;

	@FXML
	private Label labelErroEmail;

	@FXML
	private Label labelErroBirthDate;

	@FXML
	private Label labelErroBaseSalary;

	@FXML
	private Button btSave;

	@FXML
	private Button btCancel;

	private ObservableList<Department> obsList;

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	public void setServices(SellerService service, DepartmentService departmentService) {
		this.service = service;
		this.departmentService = departmentService;
	}

	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("Entity est� nulo");
		}
		if (service == null) {
			throw new IllegalStateException("Service est� nulo");
		}
		try {
			entity = getFormData();
			service.saveOrUpdate(entity);
			notifyDataChangeListeners();
			Utils.currentStage(event).close();
		} catch (ValidationException e) {
			setErrorMessagens(e.getErrors());
		} catch (DbException e) {
			Alerts.showAlert("Erro saving objcect", null, e.getMessage(), AlertType.ERROR);
		}
	}

	private void notifyDataChangeListeners() {
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChange();
		}

	}

	private Seller getFormData() {
		Seller obj = new Seller();

		ValidationException exception = new ValidationException("Validation error");

		obj.setId(Utils.tryParseToInt(txId.getText()));

		if (txName.getText() == null || txName.getText().trim().equals("")) {
			exception.addError("name", "O campo n�o pode ser vazio");
		}
		obj.setName(txName.getText());
		
		if (txEmail.getText() == null || txEmail.getText().trim().equals("")) {
			exception.addError("email", "O campo n�o pode ser vazio");
		}
		obj.setEmail(txEmail.getText());
		
		if(dpBirthDate.getValue() == null) {
			exception.addError("birthDate", "O campo n�o pode ser vazio");
		}
		else {
		      Instant instant = Instant.from(dpBirthDate.getValue().atStartOfDay(ZoneId.systemDefault()));
		      obj.setBirthDate(Date.from(instant));
		}
		
		if (txBaseSalary.getText() == null || txBaseSalary.getText().trim().equals("")) {
			exception.addError("baseSalary", "O campo n�o pode ser vazio");
		}
		obj.setBaseSalary(Utils.tryParseToDouble(txBaseSalary.getText()));
		
		obj.setDepartment(comboBoxDepartment.getValue());
		
		if (exception.getErrors().size() > 0) {
			throw exception;
		}
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
		Constraints.setTextFieldMaxLength(txName, 70);
		Constraints.setTextFieldDouble(txBaseSalary);
		Constraints.setTextFieldMaxLength(txEmail, 60);
		Utils.formatDatePicker(dpBirthDate, "dd/MM/yyyy");
		
		initializeComboBoxDepartment();

	}

	public void updateFormDate() {

		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		txId.setText(String.valueOf(entity.getId()));
		txName.setText(entity.getName());
		txEmail.setText(entity.getEmail());
		Locale.setDefault(Locale.US);
		txBaseSalary.setText(String.format("%.2f", entity.getBaseSalary()));

		if (entity.getBirthDate() != null) {
			dpBirthDate.setValue(LocalDate.ofInstant(entity.getBirthDate().toInstant(), ZoneId.systemDefault()));
		}
		if(entity.getDepartment() == null) {
			comboBoxDepartment.getSelectionModel().selectFirst();
		}
		else {
		     comboBoxDepartment.setValue(entity.getDepartment());
		}
	}

	public void loadAssociatedObjects() {
		if (departmentService == null) {
			throw new IllegalStateException("DepartmentService est� nulo");
		}
		List<Department> list = departmentService.findAll();
		obsList = FXCollections.observableArrayList(list);
		comboBoxDepartment.setItems(obsList);

	}

	private void setErrorMessagens(Map<String, String> errors) {
		Set<String> fields = errors.keySet();

		labelErroName.setText(fields.contains("name") ? errors.get("name") : "" );
		labelErroEmail.setText(fields.contains("email") ? errors.get("email") : "" );
		labelErroBirthDate.setText(fields.contains("birthDate") ? errors.get("birtheDate") : "" );
		labelErroBaseSalary.setText(fields.contains("baseSalary") ? errors.get("baseSalary") : "" );
		
		
	}

	private void initializeComboBoxDepartment() {
		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
			@Override
			protected void updateItem(Department item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? "" : item.getName());
			}
		};
		comboBoxDepartment.setCellFactory(factory);
		comboBoxDepartment.setButtonCell(factory.call(null));
	}

}
