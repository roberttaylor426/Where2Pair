import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.where2pair.venue.OpenHoursJsonMarshaller
import org.where2pair.venue.Venue
import org.where2pair.venue.VenueJsonMarshaller
import org.where2pair.venue.VenueRepository
import org.where2pair.venue.find.*
import org.where2pair.venue.persist.HashMapVenueRepository
import org.where2pair.venue.persist.MongoVenueRepository
import org.where2pair.venue.save.SaveVenueController
import org.where2pair.venue.save.VenueSaveOrUpdater
import org.where2pair.venue.show.ShowVenueController

import static ratpack.groovy.Groovy.ratpack

class Where2PairModule extends AbstractModule {
    @Provides
    VenueJsonMarshaller createVenueJsonMarshaller() {
        OpenHoursJsonMarshaller openHoursJsonMarshaller = new OpenHoursJsonMarshaller()
        new VenueJsonMarshaller(openHoursJsonMarshaller: openHoursJsonMarshaller)
    }

    @Provides
    ShowVenueController createShowVenueController(VenueRepository venueRepository, VenueJsonMarshaller venueJsonMarshaller) {
        new ShowVenueController(venueRepository: venueRepository, venueJsonMarshaller: venueJsonMarshaller)
    }

    @Provides
    SaveVenueController createSaveVenueController(VenueRepository venueRepository, VenueJsonMarshaller venueJsonMarshaller) {
        VenueSaveOrUpdater venueSaveOrUpdater = new VenueSaveOrUpdater(venueRepository: venueRepository)
        new SaveVenueController(venueSaveOrUpdater: venueSaveOrUpdater, venueJsonMarshaller: venueJsonMarshaller)
    }

    @Provides
    FindVenueController createFindVenueController(VenueRepository venueRepository, VenueJsonMarshaller venueJsonMarshaller) {
        VenueFinder venueFinder = new VenueFinder(venueRepository: venueRepository)
        TimeProvider timeProvider = new TimeProvider()
        LocationsCriteriaParser locationsCriteriaParser = new LocationsCriteriaParser()
        new FindVenueController(timeProvider: timeProvider, locationsCriteriaParser: locationsCriteriaParser, venueFinder: venueFinder, venueJsonMarshaller: venueJsonMarshaller)
    }

    @Provides @Singleton
    HashMapVenueRepository createVenueRepository(MongoVenueRepository mongoVenueRepository) {
        new HashMapVenueRepository(mongoVenueRepository.getAll())
    }

    @Override
    protected void configure() {
    }
}

ratpack {
    modules {
        register new Where2PairModule()
    }

    handlers {
        get {
            response.send "Welcome to Where2Pair!!! Your installation is working. For a list of the endpoints available, please see the documentation."
        }
        prefix("venues") {
            get { ShowVenueController showVenueController ->
                def venues = showVenueController.showAll()
                renderResult(response, venues)
            }
            get("nearest") { FindVenueController findVenueController ->
				def queryParams = squashLocationQueryParamValuesIntoList(request.queryParams)
                def venues = findVenueController.findNearest(queryParams)
                renderResult(response, venues)
            }
        }
        prefix("venue") {
            get(":venueId") { ShowVenueController showVenueController ->
                def venue = showVenueController.show(Long.parseLong(pathTokens.venueId))
                renderResult(response, venue)
            }
            post { SaveVenueController saveVenueController ->
                def json = new JsonSlurper().parseText(request.body.text)
                def venue = saveVenueController.save(json)
                renderResult(response, venue)
            }
        }
    }
}

def renderResult(response, ErrorResponse errorResponse) {
    response.status(errorResponse.status, errorResponse.message)
    response.send(errorResponse.message)
}

def renderResult(response, result) {
    String json = new JsonBuilder(result).toString()
    response.send("application/json", json)
}

def squashLocationQueryParamValuesIntoList(queryParams) {
	queryParams.collectEntries { key, value ->
		if (key == 'location') {
			return [(key): queryParams.getAll(key)]
		}
		[(key): value]
	}
}