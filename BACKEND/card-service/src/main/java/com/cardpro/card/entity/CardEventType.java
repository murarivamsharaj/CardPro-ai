package com.cardpro.card.entity;

/**
 * The kinds of engagement recorded against a card profile:
 * <ul>
 *   <li>{@link #VIEW} / {@link #PAGE_VIEW} — a page impression of the public
 *       card viewer (PAGE_VIEW is what the public events endpoint stores)</li>
 *   <li>{@link #CLICK} / {@link #SOCIAL_CLICK} / {@link #BUTTON_CLICK} — a
 *       visitor tapping one of the card's social / portfolio links or quick actions</li>
 *   <li>{@link #VCF_DOWNLOAD} — a visitor downloaded the vCard contact file</li>
 * </ul>
 * Stored as a plain string column ({@code @Enumerated(EnumType.STRING)}) so the
 * values stay readable in the database and survive renames safely.
 */
public enum CardEventType {
    VIEW,
    PAGE_VIEW,
    CLICK,
    SOCIAL_CLICK,
    BUTTON_CLICK,
    VCF_DOWNLOAD
}
