package org.where2pair

import org.where2pair.DailyOpeningTimes.SimpleTime

class VenueFinder {

	VenueRepository venueRepository
	DistanceCalculator distanceCalculator
	
	List findNearestTo(OpenTimesCriteria openTimesCriteria, Coordinates... coordinates) {
		List openVenues = venueRepository.getAll().findAll { Venue venue -> 
			venue.isOpen(openTimesCriteria) 
		}
		
		List sortedVenues = sortVenuesByDistance(openVenues, coordinates)
		
		restrictTo50Results(sortedVenues)
	}

	private List sortVenuesByDistance(List openVenues, Coordinates... coordinates) {
		List venuesWithDistance = openVenues.collect { Venue venue -> 
			new VenueWithDistance(venue: venue, distanceInKm: distanceCalculator.distanceInKmTo(venue, coordinates))
		}
		
		venuesWithDistance.sort { VenueWithDistance venue -> 
			venue.distanceInKm 
		}
	}

	private List restrictTo50Results(List venues) {
		venues.size() > 50 ? venues[0..49] : venues
	}
	
}
