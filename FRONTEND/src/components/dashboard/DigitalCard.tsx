import React, { useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';
// import VCard from 'vcard-creator';

// 1. Define the TypeScript interface for the expected props
interface CardProps {
    card: {
        id: string;
        firstName: string;
        lastName: string;
        jobTitle: string;
        phoneNumber: string;
        email: string;
    };
}

// 2. Apply the interface to the component signature
export default function DigitalCard({ card }: CardProps) {
    const [isFlipped, setIsFlipped] = useState(false);

    const publicCardUrl = `https://frontend-two-kohl-2uyfslpb5o.vercel.app/cards/${card.id}`;

const handleDownloadVCard = (e: React.MouseEvent<HTMLButtonElement>) => {
        e.stopPropagation(); 

        // Generate the standard vCard format manually using a template string
        const vcfContent = `BEGIN:VCARD
VERSION:3.0
N:${card.lastName};${card.firstName};;;
FN:${card.firstName} ${card.lastName}
ORG:${card.jobTitle}
TEL;TYPE=WORK,VOICE:${card.phoneNumber}
EMAIL;TYPE=WORK:${card.email}
URL:${publicCardUrl}
END:VCARD`;

        // Create the downloadable file
        const blob = new Blob([vcfContent], { type: 'text/vcard;charset=utf-8' });
        const url = window.URL.createObjectURL(blob);
        
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `${card.firstName}_${card.lastName}_Contact.vcf`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    return (
        <div 
            className="w-[350px] h-[220px] mx-auto my-5 cursor-pointer [perspective:1000px]" 
            onClick={() => setIsFlipped(!isFlipped)}
        >
            <div 
                className={`relative w-full h-full transition-all duration-700 ease-out [transform-style:preserve-3d] ${
                    isFlipped ? '[transform:rotateY(180deg)_scale(1.05)]' : ''
                }`}
            >
                {/* FRONT OF THE CARD */}
                <div className="absolute w-full h-full [backface-visibility:hidden] rounded-2xl shadow-xl flex flex-col items-center justify-center bg-gradient-to-br from-purple-600 to-blue-500 text-white p-6 text-center">
                    <h2 className="text-2xl font-bold tracking-wide">{card.firstName} {card.lastName}</h2>
                    <p className="text-sm text-purple-200 font-medium mt-1">{card.jobTitle}</p>
                    <p className="text-lg font-semibold mt-4">{card.phoneNumber}</p>
                    <div className="absolute bottom-4 text-xs text-purple-200/70 animate-pulse">
                        Click to flip & connect
                    </div>
                </div>

                {/* BACK OF THE CARD */}
                <div className="absolute w-full h-full [backface-visibility:hidden] rounded-2xl shadow-xl flex flex-col items-center justify-center bg-white text-gray-800 border-2 border-gray-100 [transform:rotateY(180deg)] p-6">
                    <h3 className="text-sm font-bold text-gray-500 uppercase tracking-wider mb-3">
                        Scan to View
                    </h3>
                    
                    <div className="p-2 bg-white rounded-lg shadow-sm border border-gray-100">
                        <QRCodeSVG value={publicCardUrl} size={100} />
                    </div>
                    
                    <button 
                        onClick={handleDownloadVCard} 
                        className="mt-4 px-6 py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-sm font-bold rounded-lg transition-colors shadow-md hover:shadow-lg"
                    >
                        Save to Contacts
                    </button>
                </div>
            </div>
        </div>
    );
}