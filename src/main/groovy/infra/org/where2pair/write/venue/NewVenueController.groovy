package org.where2pair.write.venue

import groovy.json.JsonSlurper
import groovy.transform.TupleConstructor
import org.where2pair.common.venue.JsonResponse

@TupleConstructor
class NewVenueController {

    NewVenueService newVenueService

    JsonResponse save(String venueJson) {
        try {
            def venueJsonMap = parseJson(venueJson)
            ensureJsonInCorrectFormat(venueJsonMap)
            def venueId = newVenueService.save(venueJsonMap)
            return okResponse(venueId)
        } catch (InvalidVenueJsonException e) {
            return badRequestResponse(e)
        }
    }

    private parseJson(String venueJson) {
        new JsonSlurper().parseText(venueJson)
    }

    private void ensureJsonInCorrectFormat(venueJsonMap) {
        if (!(venueJsonMap instanceof Map))
            throw new InvalidVenueJsonException('Venue json not in the expected format')
    }

    private JsonResponse okResponse(NewVenueId venueId) {
        JsonResponse.validJsonResponse([venueId: venueId.encode()])
    }

    private JsonResponse badRequestResponse(InvalidVenueJsonException e) {
        JsonResponse.badRequest(e.message)
    }

}
