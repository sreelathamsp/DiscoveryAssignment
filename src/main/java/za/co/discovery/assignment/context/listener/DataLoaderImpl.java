package za.co.discovery.assignment.context.listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import za.co.discovery.assignment.exception.SamePlanetsNotAllowedException;
import za.co.discovery.assignment.orm.Planet;
import za.co.discovery.assignment.orm.Route;
import za.co.discovery.assignment.repository.PlanetRepository;
import za.co.discovery.assignment.repository.RouteRepository;


/**
 * Read Excel data file on the classpath to load contents into database
 */
@Component
public class DataLoaderImpl implements DataLoader {

	@Autowired
	PlanetRepository planetRepo;

	@Autowired
	RouteRepository routesRepo;

	@Value(value = "${excel.data.file.location}")
	private String EXCEL_DATA_FILE;

	
	static final Logger LOG = org.apache.log4j.Logger.getLogger(DataLoaderImpl.class);

	
	/**
	 * When the Spring application context is started the below method will catch the lifecycle 
	 * ContextStartedEvent and then invoke readXlsDataFile()
	 * @Param startEvent The event fired when the Spring context is initialized
	 */
	@EventListener
	public void onApplicationEvent(ContextStartedEvent startEvent) {
		readXlsDataFile();
	}

	/**
	 * Entry method to process the XLS worksheets starting with the planets sheet and then proceeding to the routes sheet
	 */
	@Override
	public void readXlsDataFile() {
		try {
			Workbook workbook = createWorkBook();
			processPlanetSheet(workbook);
			processRoutesSheet(workbook);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();

		}
	}
	
	/**
	 * Processes the first work sheet(planets) and then saves each record into the derby DB
	 */

	
	private void processPlanetSheet(Workbook workbook) throws FileNotFoundException, IOException {
		Sheet planetsSheet = getSheet(workbook, 0); // Read First worksheet for planets information
		Iterator<Row> iterator = planetsSheet.iterator();
		while (iterator.hasNext()) {
			Row currentRow = iterator.next();
			Cell planetIdCell = currentRow.getCell(0);
			Cell planetNameCell = currentRow.getCell(1);
			if ((planetIdCell.getCellTypeEnum() == CellType.STRING)
					&& (planetNameCell.getCellTypeEnum() == CellType.STRING)) {
				savePlanets(planetIdCell.getStringCellValue(), planetNameCell.getStringCellValue());
			}
		}
	}

	/**
	 * Processes the second work sheet(routes) and then saves each record into the derby DB
	 */
	
	private void processRoutesSheet(Workbook workbook) throws FileNotFoundException, IOException {
		Sheet planetSheet = getSheet(workbook, 1);
		Iterator<Row> it = planetSheet.iterator();
		while (it.hasNext()) {
			Row row = it.next();
			Cell idCell = row.getCell(0);
			Cell sourceCell = row.getCell(1);
			Cell destCell = row.getCell(2);
			Cell weightCell = row.getCell(3);
			short routeID = 0;
			String source = " ";
			String destination = " ";
			float distance = 0.0f;
			if (idCell.getCellTypeEnum() == CellType.NUMERIC) {
				routeID = (short) idCell.getNumericCellValue();
			} else {
				continue;
			}
			if (sourceCell.getCellTypeEnum() == CellType.STRING) {
				source = sourceCell.getStringCellValue();
			}
			if (destCell.getCellTypeEnum() == CellType.STRING) {
				destination = destCell.getStringCellValue();
			}
			if (weightCell.getCellTypeEnum() == CellType.NUMERIC) {
				distance = (float) weightCell.getNumericCellValue();
			}
			saveRoute(routeID, source, destination, distance);
		}
	}

	private void saveRoute(short routeID, String source, String destination, float distance) {
		try {
			if (source != destination) {
				persistRoute(routeID, source, destination, distance);
			}
		} catch (SamePlanetsNotAllowedException e) {
			LOG.info(e.getLocalizedMessage() + "for route id=" + routeID + "got  " + source + " and " + destination);
		}
	}
	
	/**
	 * Creates an XLS work book based on specifed XLS file path
	 * @return Workbook XLS workbook
	 * @throws FileNotFoundException,IOException
	 */

	private Workbook createWorkBook() throws FileNotFoundException, IOException {
		FileInputStream xlsFile = new FileInputStream(new File(EXCEL_DATA_FILE));
		return new XSSFWorkbook(xlsFile);
	}
	
	/**
	 * Creates an XLS work book based on specifed XLS file path
	 */

	private Sheet getSheet(Workbook workBook, int index) throws FileNotFoundException, IOException {
		return workBook.getSheetAt(index);
	}
	
	/**
	 * Saves planets into the derby DB
	 * @param node planetID
	 *  @param descr planetDescr
	 */

	private void savePlanets(String node, String descr) {
		if (!node.contains("Node")) {
			planetRepo.save(new Planet(node, descr));
			LOG.info("Saved Planet ==> {Planet Node: " + node + "   Name: " + descr + "}");
		}
	}

	
	/**
	 * Saves routes into the derby DB
	 */
	private void persistRoute(Short routeId, String origin, String planetDest, float distance)
			throws SamePlanetsNotAllowedException {
		Planet source = planetRepo.findOne(origin);
		Planet dest = planetRepo.findOne(planetDest);
		if ((source != null) && (dest != null)) {
			routesRepo.save(new Route(routeId, source, dest, distance));
			LOG.info("Saved Route ==> {Route ID: " + routeId + "  Source : " + source.getPlanetID()+ " Dest : " + dest.getPlanetID() + " Distance " + distance + "}");
		}
	}
}
